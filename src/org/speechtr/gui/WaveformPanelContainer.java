package org.speechtr.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.speechtr.data.AudioInfo;

public class WaveformPanelContainer extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final transient AudioInfo audioInfo;
	private static SingleWaveformPanel waveformPanel;
	private static JLabel secondsPointerLabel;

	private static final Logger DEBUG_LOG = Logger.getLogger("debuger");

	public WaveformPanelContainer(final AudioInputStream audioInputStream) throws IOException
	{
		DEBUG_LOG.debug("WaveformPanelContainer object initialization");
		setLayout(new GridLayout(0, 1));
		audioInfo = new AudioInfo(audioInputStream);

		waveformPanel = new SingleWaveformPanel(audioInfo);
		add(createChannelDisplay(waveformPanel));
	}

	public static SingleWaveformPanel getWaveformPanel()
	{
		return waveformPanel;
	}

	public static JLabel getSecondsPointerLabel()
	{
		return secondsPointerLabel;
	}

	private static JComponent createChannelDisplay(final SingleWaveformPanel waveformPanel)
	{
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(waveformPanel, BorderLayout.CENTER);

		final JPanel labelPanel = new JPanel();
		secondsPointerLabel = new JLabel();
		labelPanel.add(secondsPointerLabel, FlowLayout.LEFT);
		final JLabel label = new JLabel("Waveform graph:");
		labelPanel.add(label, FlowLayout.LEFT);
		panel.add(labelPanel, BorderLayout.NORTH);

		return panel;
	}

}
