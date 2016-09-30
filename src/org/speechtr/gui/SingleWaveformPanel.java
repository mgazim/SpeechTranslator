package org.speechtr.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.speechtr.data.AudioInfo;

public class SingleWaveformPanel extends JPanel implements MouseMotionListener, MouseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Color BACKGROUND_COLOR = Color.white;
	private static final Color REFERENCE_LINE_COLOR = Color.black;
	private static final Color WAVEFORM_COLOR = Color.black;
	private static final Color SPLIT_POINTS_COLOR = Color.red;
	private static final Color PLAYING_LINE_COLOR = Color.green;

	private final AudioInfo helper;
	private volatile int pointerXLocation = -1;
	private int playingLineX = -1;
	private List<Integer> splitPoints = new ArrayList<>();

	public SingleWaveformPanel(final AudioInfo helper)
	{
		this.helper = helper;
		setBackground(BACKGROUND_COLOR);
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	@Override
	protected void paintComponent(final Graphics g)
	{
		super.paintComponent(g);

		final int lineHeight = getHeight() / 2;
		g.setColor(REFERENCE_LINE_COLOR);
		g.drawLine(0, lineHeight, getWidth(), lineHeight);

		drawWaveform(g, helper.getAudio(0));
		drawPlayingLine(g);
		drawPointer(g);
		drawSplitPoints(g);
	}

	private void drawSplitPoints(final Graphics g)
	{
		for (int i = 0; i < splitPoints.size(); i++)
		{
			g.setColor(SPLIT_POINTS_COLOR);
			g.drawLine(splitPoints.get(i), 0, splitPoints.get(i), getHeight());
		}
	}

	private void drawPlayingLine(final Graphics g)
	{
		g.setColor(PLAYING_LINE_COLOR);
		if (playingLineX >= 0)
		{
			g.drawLine(playingLineX, 0, playingLineX, getHeight());
		}
	}

	private void drawPointer(final Graphics g)
	{
		g.setColor(REFERENCE_LINE_COLOR);
		if (pointerXLocation >= 0)
		{
			final double currSeconds = BigDecimal.valueOf(framesToSeconds(locationToFrames(pointerXLocation))).setScale(2, RoundingMode.UP)
					.doubleValue();
			WaveformPanelContainer.getSecondsPointerLabel()
					.setText(currSeconds + " from " + BigDecimal.valueOf(helper.getAudioDuration()).setScale(2, RoundingMode.UP));
			g.drawLine(pointerXLocation, 0, pointerXLocation, getHeight());
		}
	}

	private void drawWaveform(final Graphics g, final int[] samples)
	{
		if (samples == null)
		{
			return;
		}

		int oldX = 0;
		int oldY = getHeight() / 2;
		int xIndex = 0;

		final int increment = helper.getIncrement(helper.getXScaleFactor(getWidth()));
		g.setColor(WAVEFORM_COLOR);

		int t = 0;

		for (; t < increment; t += increment)
		{
			g.drawLine(oldX, oldY, xIndex, oldY);
			xIndex++;
			oldX = xIndex;
		}

		for (; t < samples.length; t += increment)
		{
			final double scaleFactor = helper.getYScaleFactor(getHeight());
			final double scaledSample = samples[t] * scaleFactor;
			final int y = (int) ((getHeight() / 2) - (scaledSample));
			g.drawLine(oldX, oldY, xIndex, y);

			xIndex++;
			oldX = xIndex;
			oldY = y;
		}
	}

	public double locationToFrames(final int locationX)
	{
		return locationX / (WaveformPanelContainer.getWaveformPanel().getWidth() / (double) helper.getSamplesContainer().length);
	}

	public double framesToSeconds(final double frames)
	{
		return frames / helper.getAudioInputStream().getFormat().getSampleRate();
	}

	public void setPlayingLineX(final int playingLineX)
	{
		this.playingLineX = playingLineX;
		repaint();
	}

	public void setSplitPoints(final List<Integer> splitPoints)
	{
		this.splitPoints = splitPoints;
	}

	public List<Integer> getSplitPoints()
	{
		return this.splitPoints;
	}

	public void removeLastSplitPoint()
	{
		this.splitPoints.remove(this.splitPoints.size() - 1);
	}

	public AudioInfo getHelper()
	{
		return helper;
	}

	@Override
	public void mouseDragged(final MouseEvent e)
	{
	}

	@Override
	public void mouseMoved(final MouseEvent e)
	{
		pointerXLocation = e.getX();
		repaint();
	}

	@Override
	public void mouseClicked(final MouseEvent e)
	{
		splitPoints.add(e.getX());
		repaint();
	}

	@Override
	public void mouseEntered(final MouseEvent e)
	{
	}

	@Override
	public void mouseExited(final MouseEvent e)
	{
		WaveformPanelContainer.getSecondsPointerLabel().setText("");
		pointerXLocation = -1;
		repaint();
	}

	@Override
	public void mousePressed(final MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(final MouseEvent e)
	{
	}
}
