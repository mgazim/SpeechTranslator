package org.speechtr.data;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;

public class AudioInfo
{
	private static final int NUM_BITS_PER_BYTE = 8;

	private final AudioInputStream audioInputStream;
	private int[] samplesContainer;

	private int sampleMax = 0;
	private int sampleMin = 0;
	private double biggestSample;

	private final double audioDuration;

	public AudioInfo(final AudioInputStream aiStream) throws IOException
	{
		this.audioInputStream = new AudioInputStream(aiStream, aiStream.getFormat(), aiStream.getFrameLength());
		createSampleArrayCollection();

		audioDuration = ((double) aiStream.getFrameLength()) / aiStream.getFormat().getFrameRate();
	}

	public int getNumberOfChannels()
	{
		final int numBytesPerSample = audioInputStream.getFormat().getSampleSizeInBits() / NUM_BITS_PER_BYTE;
		return audioInputStream.getFormat().getFrameSize() / numBytesPerSample;
	}

	private void createSampleArrayCollection() throws IOException
	{
		final byte[] bytes = new byte[(int) (audioInputStream.getFrameLength()) * audioInputStream.getFormat().getFrameSize()];

		final int read = audioInputStream.read(bytes);

		if (read < 0)
		{
			throw new IOException();
		}
		// convert sample bytes to channel separated 16 bit samples
		samplesContainer = getSampleArray(bytes);

		// find biggest sample. used for interpolating the yScaleFactor
		if (sampleMax > sampleMin)
		{
			biggestSample = sampleMax;
		}
		else
		{
			biggestSample = Math.abs((double) sampleMin);
		}

	}

	protected int[] getSampleArray(final byte[] eightBitByteArray)
	{
		final int[] toReturn = new int[eightBitByteArray.length / (2 * getNumberOfChannels())];
		int index = 0;

		// loop through the byte[]
		for (int t = 0; t < eightBitByteArray.length;)
		{
			// for each iteration, loop through the channels

			// do the byte to sample conversion
			// see AmplitudeEditor for more info
			final int low = eightBitByteArray[t];
			t++;
			final int high = eightBitByteArray[t];
			t++;
			final int sample = (high << 8) + (low & 0x00ff);

			if (sample < sampleMin)
			{
				sampleMin = sample;
			}
			else if (sample > sampleMax)
			{
				sampleMax = sample;
			}
			// set the value.
			toReturn[index] = sample;

			index++;
		}

		return toReturn;
	}

	public double getXScaleFactor(final int panelWidth)
	{
		return panelWidth / ((double) samplesContainer.length);
	}

	public double getYScaleFactor(final int panelHeight)
	{
		return panelHeight / (biggestSample * 2 * 1.2);
	}

	public int[] getAudio(final int channel)
	{
		return samplesContainer;
	}

	public int getIncrement(final double xScale)
	{
		return (int) (samplesContainer.length / (samplesContainer.length * xScale));
	}

	public int[] getSamplesContainer()
	{
		return samplesContainer;
	}

	public AudioInputStream getAudioInputStream()
	{
		return audioInputStream;
	}

	public double getAudioDuration()
	{
		return audioDuration;
	}

}
