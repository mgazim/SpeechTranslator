package org.speechtr.convertation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.log4j.Logger;
import org.speechtr.data.ProjectProperties;
import org.speechtr.data.ProjectProperties.NoSuchPropertyException;
import org.speechtr.data.RecognizedVoiceObject;
import org.speechtr.data.WaveAudioObject;
import org.speechtr.gui.SpeechTranslatorGui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SpeechRecognizer
{
	private static String language;
	private static volatile String oldLanguage;

	private static final Logger LOG = Logger.getRootLogger();
	private static final Logger DEBUG_LOG = Logger.getLogger("debuger");

	private static RecognizedVoiceObject recentlyRecognized;

	private SpeechRecognizer()
	{
	}

	public static RecognizedVoiceObject getConvertedText(final WaveAudioObject audio) throws IOException, IllegalStateException,
			Server502ResponseException, NullGoogleResponseException, UnknownHostException, HttpHostConnectException, NoSuchPropertyException
	{
		final StringBuilder response = sendRequest(audio);
		return fromJsonToText(response);
	}

	private static StringBuilder sendRequest(final WaveAudioObject audio)
			throws IOException, Server502ResponseException, UnknownHostException, HttpHostConnectException, NoSuchPropertyException
	{
		try
		{
			SpeechTranslatorGui.getStepLabel().setText("Sending request to Google Service...");
			final URL url = new URL(ProjectProperties.getProperty("gl.url") + getLanguage() + "&key=" + ProjectProperties.getProperty("gl.key"));
			HttpHost proxy = null;
			final String proxyHostName = ProjectProperties.getProperty("conn.proxy");

			if (!(proxyHostName == null || proxyHostName.isEmpty()))
			{
				proxy = new HttpHost(proxyHostName, 8080);
				DEBUG_LOG.debug(proxyHostName + " proxy used for connection, port " + proxy.getPort());
			}
			SpeechTranslatorGui.getStepLabel().setText("Getting response from Google Service...");
			final Response response = Request.Post(url.toString()).viaProxy(proxy).bodyByteArray(audio.getAudioBytes())
					.addHeader("content-type", ProjectProperties.getProperty("gl.wavheader")).execute();

			DEBUG_LOG.debug("Got the response from Google server");

			SpeechTranslatorGui.getStepLabel().setText("Google response processing...");
			final HttpResponse httpResponse = response.returnResponse();
			final StringBuilder content = getContentFromHttpResponse(httpResponse);

			if (httpResponse.getStatusLine().getStatusCode() != 200)
			{
				showErrorResponse(content);
				LOG.error("Got 502 error code response from Google server");
				return null;
			}
			return content;
		}
		catch (final MalformedURLException e)
		{
			LOG.error("Error in URL formation", e);
		}
		return null;
	}

	private static RecognizedVoiceObject fromJsonToText(final StringBuilder response) throws IllegalStateException, NullGoogleResponseException
	{
		DEBUG_LOG.debug("Trying to get tent from the Google response");
		response.delete(0, 13);

		if (response.length() == 0)
		{
			LOG.error("Error in getting response content. NullGoogleResponseException");
			throw new NullGoogleResponseException(
					"Google Service cannot recognize the audio or its part.\nMake sure that recognition language you set matches the audio file language");
		}

		String[] transcription;
		final JsonParser parser = new JsonParser();
		final JsonObject json = parser.parse(response.toString()).getAsJsonObject();

		final JsonArray result = json.getAsJsonArray("result");
		final JsonArray alternatives = ((JsonObject) result.get(0)).getAsJsonArray("alternative");
		transcription = new String[alternatives.size()];
		int count = 0;

		for (final JsonElement alternative : alternatives)
		{
			transcription[count] = alternative.getAsJsonObject().get("transcript").getAsString();
			count++;
		}

		return new RecognizedVoiceObject(transcription);
	}

	private static void showErrorResponse(final StringBuilder content) throws Server502ResponseException
	{
		final String messageFromServer = content.substring(content.indexOf("<ins>"), content.lastIndexOf("</ins>")).replace("<ins>", "")
				.replace("<p>", "\n").replace("</ins>", "");

		throw new SpeechRecognizer.Server502ResponseException(messageFromServer);
	}

	private static StringBuilder getContentFromHttpResponse(final HttpResponse httpResponse) throws IOException
	{
		final InputStream content = httpResponse.getEntity().getContent();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(content, "utf-8"));

		final StringBuilder result = new StringBuilder();

		reader.lines().forEach(line -> result.append(line));
		return result;
	}

	public static String getLanguage()
	{
		return language;
	}

	public static void setLanguage(final String language)
	{
		SpeechRecognizer.language = language;
	}

	public static String getOldLanguage()
	{
		return oldLanguage;
	}

	public static void setOldLanguage(final String oldLanguage)
	{
		SpeechRecognizer.oldLanguage = oldLanguage;
	}

	public static boolean langWasChanged()
	{
		return !oldLanguage.equals(language);
	}

	public static RecognizedVoiceObject getRecentlyRecognized()
	{
		return recentlyRecognized;
	}

	public static void setRecentlyRecognized(final RecognizedVoiceObject recentlyRecognized)
	{
		SpeechRecognizer.recentlyRecognized = recentlyRecognized;
	}

	public static class Server502ResponseException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Server502ResponseException(final String msg)
		{
			super(msg);
		}
	}

	public static class NullGoogleResponseException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NullGoogleResponseException(final String msg)
		{
			super(msg);
		}
	}
}
