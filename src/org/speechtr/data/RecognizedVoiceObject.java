package org.speechtr.data;

public class RecognizedVoiceObject
{

	private final String[] transcription;
	private String[] translation;

	public RecognizedVoiceObject(final String[] transcription)
	{
		this.transcription = transcription;
		compare();
	}

	public String[] getTranscription()
	{
		return transcription;
	}

	public String[] getTranslation()
	{
		return translation;
	}

	public void setTranslation(final String[] translation)
	{
		this.translation = translation;
	}

	private void compare()
	{
		for (int i = 1; i < this.transcription.length; i++)
		{
			if (this.transcription[0].equalsIgnoreCase(this.transcription[i]))
				this.transcription[i] = null;
		}
	}

	public static RecognizedVoiceObject concat(final RecognizedVoiceObject[] recognizedObjects)
	{
		final int transcCount = notNullLength(recognizedObjects[0].transcription);
		final String[] transcription = new String[transcCount];
		setNotNull(transcription);

		for (int i = 0; i < recognizedObjects.length; i++)
		{

			for (int j = 0; j < transcCount; j++)
			{
				if (recognizedObjects[i].transcription[j] == null)
				{
					continue;
				}
				transcription[j] += recognizedObjects[i].transcription[j] + " ";
			}
		}

		return new RecognizedVoiceObject(transcription);
	}

	private static void setNotNull(final String[] array)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == null)
			{
				array[i] = "";
			}
		}
	}

	private static int notNullLength(final Object[] array)
	{
		int count = 0;
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] != null)
			{
				count++;
			}
		}
		return count;
	}

	@Override
	public String toString()
	{
		final StringBuilder result = new StringBuilder();
		for (int i = 0; i < this.transcription.length; i++)
		{
			result.append(this.transcription[i] + "\n" + this.translation[i] + "\n");
		}
		if (result.length() == 0)
			return "null";

		return result.toString();
	}
}
