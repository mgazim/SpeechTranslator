package org.speechtr.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.tritonus.sampled.convert.AlawFormatConversionProvider;
import org.tritonus.sampled.convert.ImaAdpcmFormatConversionProvider;
import org.tritonus.sampled.convert.UlawFormatConversionProvider;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;

public class WaveAudioObject
{
	private final AudioFormat monoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false);
	private final AudioFormat pcmFormat = new AudioFormat(8000, 16, 1, true, false);

	private AudioInputStream audio;
	private String name;
	private AudioFormat format;

	private static final Logger LOG = Logger.getRootLogger();

	public WaveAudioObject(final String filePath) throws UnsupportedAudioFileException, IOException
	{

		this.audio = AudioSystem.getAudioInputStream(new File(filePath));

		this.name = new File(filePath).getName();
		setAudioFormat();
	}

	public WaveAudioObject(final AudioInputStream audio)
	{
		this.audio = new AudioInputStream(audio, audio.getFormat(), audio.getFrameLength());
		setAudioFormat();
	}

	private void setAudioFormat()
	{
		if (this.audio.getFormat().getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
		{
			changeEncoding(this.audio);
		}
		if ((int) this.audio.getFormat().getSampleRate() != 44100)
		{
			changeSampleRate(this.audio);
		}
		if (this.audio.getFormat().getChannels() > 1)
		{
			toMono(this.audio);
		}

		format = this.audio.getFormat();
	}

	private void changeEncoding(final AudioInputStream sourceStream)
	{
		final AudioFormat sourceFormat = sourceStream.getFormat();
		if (AudioSystem.isConversionSupported(AudioFormat.Encoding.PCM_SIGNED, sourceFormat))
		{
			TEncodingFormatConversionProvider provider;

			switch (sourceFormat.getEncoding().toString().toLowerCase())
			{
				case "ulaw":
					provider = new UlawFormatConversionProvider();
					break;
				case "alaw":
					provider = new AlawFormatConversionProvider();
					break;
				case "pcm_unsigned":
					provider = new ImaAdpcmFormatConversionProvider();
					break;
				default:
					provider = null;
					break;
			}
			if (provider != null)
				this.audio = provider.getAudioInputStream(pcmFormat, sourceStream);
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Can not convert from " + sourceFormat.getEncoding().toString() + " to singned PCM", "Error",
					JOptionPane.ERROR_MESSAGE);
			this.audio = null;
		}
	}

	private void changeSampleRate(final AudioInputStream sourceStream)
	{
		final AudioInputStream pcm = new AudioInputStream(sourceStream, sourceStream.getFormat(), sourceStream.getFrameLength());
		final AudioInputStream targetStream = AudioSystem.getAudioInputStream(monoFormat, pcm);

		this.audio = targetStream;
	}

	private void toMono(final AudioInputStream stereoStream)
	{
		try
		{

			final byte[] monoByte = getMonoByte(stereoStream);

			if (monoByte == null || monoByte.length == 0)
			{
				throw new NullPointerException();
			}

			final InputStream is = new ByteArrayInputStream(monoByte);
			final AudioInputStream monoStream = new AudioInputStream(is, monoFormat, is.available());

			this.audio = monoStream;
		}
		catch (final IOException e)
		{
			LOG.error("Error occured while working with AudioInputStream object", e);
		}
	}

	private byte[] getMonoByte(final InputStream stereoStream)
	{
		try
		{
			final byte[] buff = new byte[stereoStream.available()];
			final int read = stereoStream.read(buff);

			if (read == 0)
			{
				throw new IOException();
			}

			final byte[] mono = new byte[buff.length / 2];

			for (int i = 0; i < mono.length / 2; ++i)
			{
				final int hi = 1; // endianess
				final int lo = 0; //
				final int left = (buff[i * 4 + hi] << 8) | (buff[i * 4 + lo] & 0xff);
				final int right = (buff[i * 4 + 2 + hi] << 8) | (buff[i * 4 + 2 + lo] & 0xff);
				final int avg = (left + right) / 2;
				mono[i * 2 + hi] = (byte) ((avg >> 8) & 0xff);
				mono[i * 2 + lo] = (byte) (avg & 0xff);
			}
			return mono;
		}
		catch (final IOException e)
		{
			LOG.error("Error in working with stereo InputStream", e);
			return new byte[0];
		}
	}

	public AudioFormat getFormat()
	{
		return format;
	}

	public AudioInputStream getAudio()
	{
		return audio;
	}

	public byte[] getAudioBytes() throws IOException
	{
		final byte[] bytes = new byte[(int) (this.audio.getFrameLength() * this.format.getFrameSize())];
		final AudioInputStream stream = new AudioInputStream(this.audio, this.audio.getFormat(), this.audio.getFrameLength());
		final int read = stream.read(bytes);
		if (read < 0)
		{
			throw new IOException();
		}
		return bytes;
	}

	public String getName()
	{
		return name;
	}
}