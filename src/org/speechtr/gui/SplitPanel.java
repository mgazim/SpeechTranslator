package org.speechtr.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.apache.log4j.Logger;
import org.speechtr.data.WaveAudioObject;

public class SplitPanel extends JPanel implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getRootLogger();
	private static final Logger DEBUG_LOG = Logger.getLogger("debuger");

	private static JLabel label;
	private transient Thread playingLineRegulator;

	private transient Clip clip;
	{
		try
		{
			clip = AudioSystem.getClip();
		}
		catch (final LineUnavailableException e)
		{
			LOG.error("Problem in creating Clip object in SplitPanel class", e);
		}

	}

	private List<Integer> splitIndexes = new ArrayList<>();

	public SplitPanel(final String filePath) throws IOException, UnsupportedAudioFileException
	{
		DEBUG_LOG.debug("SplitPanel object initialization");
		final WaveAudioObject audio = new WaveAudioObject(filePath);
		setBounds(100, 100, 555, 189);
		final JPanel panel = new JPanel();
		final JPanel panel1 = new JPanel();

		panel.setLayout(new BorderLayout(0, 0));
		final WaveformPanelContainer waveformPanelContainer = new WaveformPanelContainer(audio.getAudio());
		panel.add(waveformPanelContainer);

		final JButton btnPlay = new JButton("");
		final JButton btnPause = new JButton("");
		final JButton btnStop = new JButton("");
		btnStop.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				DEBUG_LOG.debug("Stop audio playing");
				btnPlay.setEnabled(true);
				btnStop.setEnabled(false);
				btnPause.setEnabled(false);

				clip.close();
			}
		});

		btnPlay.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				DEBUG_LOG.debug("Play audio");
				try
				{
					final WaveAudioObject audio1 = new WaveAudioObject(filePath);
					if (audio1.getAudio() == null)
					{
						return;
					}
					btnPlay.setEnabled(false);
					btnPause.setEnabled(true);
					btnStop.setEnabled(true);
					if (!clip.isOpen())
					{
						clip.open(audio1.getAudio());
					}
					clip.addLineListener(new LineListener()
					{
						@Override
						public void update(final LineEvent event)
						{
							if (event.getType() == LineEvent.Type.START)
							{
								playingLineRegulator = new Thread()
								{
									@Override
									public void run()
									{
										while (clip.isRunning())
										{
											final double currDuration = BigDecimal.valueOf(((double) clip.getMicrosecondPosition()) / 1_000_000)
													.setScale(2, RoundingMode.UP).doubleValue();
											label.setText(Double.toString(currDuration));

											final int framePos = (int) (clip.getFramePosition()
													* (WaveformPanelContainer.getWaveformPanel().getWidth() / (double) clip.getFrameLength()));
											WaveformPanelContainer.getWaveformPanel().setPlayingLineX(framePos);
										}
									}
								};
								playingLineRegulator.start();
							}
							if (event.getType() == LineEvent.Type.CLOSE)
							{
								DEBUG_LOG.debug("Audio was closed");
								label.setText("0.0");
								WaveformPanelContainer.getWaveformPanel().setPlayingLineX(0);
							}
							if (event.getType() == LineEvent.Type.STOP && clip.getFramePosition() == clip.getFrameLength())
							{
								DEBUG_LOG.debug("Audio was stopped");
								btnPlay.setEnabled(true);
								btnStop.setEnabled(false);
								btnPause.setEnabled(false);

								clip.close();
							}
						}
					});
					clip.start();

				}
				catch (LineUnavailableException | IOException e1)
				{
					LOG.error("Error occured while trying to play audio file", e1);
					JOptionPane.showMessageDialog(null, "Error while playing audio file", "Error", JOptionPane.ERROR_MESSAGE);
				}
				catch (final UnsupportedAudioFileException e2)
				{

					LOG.error("Audio file format is mot supported", e2);
					JOptionPane.showMessageDialog(null,
							"Audio file format is not supported.\nOnly WAVE audio files in PCM or ULAW (ALAW) encoding is allowed", "System error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnPlay.setIcon(new ImageIcon("icons/play.png"));

		btnPause.setEnabled(false);
		btnPause.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				DEBUG_LOG.debug("Audio was paused");
				clip.stop();
				btnPlay.setEnabled(true);
			}
		});
		btnPause.setIcon(new ImageIcon("icons/pause.png"));

		final JButton btnOk = new JButton("Ok");
		btnOk.addActionListener(this);

		final JButton btnRemoveSplitPoints = new JButton("Remove all");
		btnRemoveSplitPoints.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ev)
			{
				WaveformPanelContainer.getWaveformPanel().setSplitPoints(new ArrayList<>());
				splitIndexes = new ArrayList<>();
				WaveformPanelContainer.getWaveformPanel().repaint();
				DEBUG_LOG.debug("All split points were removed");
			}
		});

		label = new JLabel("0.0");

		btnStop.setEnabled(false);
		btnStop.setIcon(new ImageIcon("icons/stop.png"));

		final JButton btnUndo = new JButton();
		btnUndo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				if (!WaveformPanelContainer.getWaveformPanel().getSplitPoints().isEmpty())
				{
					WaveformPanelContainer.getWaveformPanel().removeLastSplitPoint();
					WaveformPanelContainer.getWaveformPanel().repaint();
					DEBUG_LOG.debug("Undo split point mark");
				}
			}
		});
		btnUndo.setIcon(new ImageIcon("icons/undo.png"));

		final GroupLayout glPanel1 = new GroupLayout(panel1);
		glPanel1.setHorizontalGroup(glPanel1.createParallelGroup(Alignment.LEADING).addGroup(glPanel1.createSequentialGroup().addContainerGap()
				.addComponent(btnPlay, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(btnPause, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(btnStop, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(label, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, 92, Short.MAX_VALUE).addComponent(btnUndo).addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(btnRemoveSplitPoints).addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(btnOk, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE).addContainerGap()));
		glPanel1.setVerticalGroup(glPanel1.createParallelGroup(Alignment.LEADING)
				.addGroup(glPanel1.createSequentialGroup()
						.addGroup(glPanel1.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnOk, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnRemoveSplitPoints, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnUndo, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
						.addContainerGap())
				.addComponent(btnStop, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
				.addComponent(btnPause, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
				.addComponent(btnPlay, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE).addGroup(glPanel1.createSequentialGroup()
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(label).addContainerGap()));
		panel1.setLayout(glPanel1);
		final GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup()
				.addGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(panel, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(
								groupLayout.createSequentialGroup().addGap(9).addComponent(panel1, GroupLayout.PREFERRED_SIZE, 536, Short.MAX_VALUE)))
				.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addGap(4)
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(7)));
		setLayout(groupLayout);
	}

	public Clip getClip()
	{
		return clip;
	}

	public List<Integer> getSplitIndexes()
	{
		return splitIndexes;
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		final ArrayList<Integer> splitPoints = (ArrayList<Integer>) WaveformPanelContainer.getWaveformPanel().getSplitPoints();
		for (final int locationX : splitPoints)
		{
			final int index = toSplitIndex((int) WaveformPanelContainer.getWaveformPanel().locationToFrames(locationX));
			splitIndexes.add(index);
		}
		splitIndexes.sort(null);

		if (!isLessThan10Sec())
		{
			DEBUG_LOG.debug("Audio parts are not less than 10 seconds");
			JOptionPane.showMessageDialog(this, "All audio parts have to be less than or equal 10 seconds", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		getJDialogComponent(this).dispose();
		DEBUG_LOG.debug("All split point was taken. Close dialog pane");
	}

	private boolean isLessThan10Sec()
	{
		final double total = BigDecimal.valueOf(WaveformPanelContainer.getWaveformPanel().getHelper().getAudioDuration()).setScale(2, RoundingMode.UP)
				.doubleValue();
		double fromStart = 0.0;
		for (final int index : splitIndexes)
		{
			final double currPartDuration = BigDecimal
					.valueOf(WaveformPanelContainer.getWaveformPanel().framesToSeconds(
							index / WaveformPanelContainer.getWaveformPanel().getHelper().getAudioInputStream().getFormat().getFrameSize()))
					.setScale(2, RoundingMode.UP).doubleValue();
			if ((currPartDuration - fromStart) > 10d)
			{
				return false;
			}
			fromStart = currPartDuration;
		}
		if (Double.compare(total, fromStart) != 0)
		{
			if ((total - fromStart) > 10d)
			{
				return false;
			}
		}
		return true;
	}

	private JDialog getJDialogComponent(final Component comp)
	{
		if (!(comp instanceof JDialog))
		{
			return getJDialogComponent(comp.getParent());
		}
		else
		{
			return (JDialog) comp;
		}
	}

	private int toSplitIndex(final int frames)
	{
		return WaveformPanelContainer.getWaveformPanel().getHelper().getAudioInputStream().getFormat().getFrameSize() * frames;
	}
}
