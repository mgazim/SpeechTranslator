package org.speechtr.translation;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.log4j.Logger;
import org.speechtr.data.ProjectProperties;
import org.speechtr.data.ProjectProperties.NoSuchPropertyException;
import org.speechtr.data.RecognizedVoiceObject;
import org.speechtr.gui.SpeechTranslatorGui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author mgazim
 * 
 *
 */
public class Translator
{
	private static String langFrom;
	private static String langTo;

	private static final Logger LOG = Logger.getRootLogger();
	private static final Logger DEBUG_LOG = Logger.getLogger("debuger");

	private Translator()
	{
	}

	public static void translate(final RecognizedVoiceObject toTranslate)
			throws UnknownHostException, HttpHostConnectException, NoSuchPropertyException
	{
		try
		{
			DEBUG_LOG.debug("Trying the translation");
			SpeechTranslatorGui.getStepLabel().setText("Sending requset to Yandex.Translate service...");
			final URL url = new URL(
					ProjectProperties.getProperty("ya.url") + ProjectProperties.getProperty("ya.key") + "&text=" + "&lang=" + getLanguage());
			HttpHost proxy = null;
			final String proxyHostName = ProjectProperties.getProperty("conn.proxy");

			if (!(proxyHostName == null || proxyHostName.isEmpty()))
			{
				proxy = new HttpHost(proxyHostName, 8080);
				DEBUG_LOG.debug(proxyHostName + " proxy used for connection, port " + proxy.getPort());
			}
			final String[] transcriptions = toTranslate.getTranscription();
			final String[] translations = new String[transcriptions.length];

			int count = 0;
			for (final String transcr : transcriptions)
			{
				if (transcr == null)
				{
					continue;
				}

				final StringBuilder urlString = new StringBuilder(url.toString());
				urlString.insert(urlString.indexOf("&text=") + 6, transcr);

				SpeechTranslatorGui.getStepLabel()
						.setText("Getting response from Yandex.Translate service (" + (count + 1) + " from " + transcriptions.length + ")...");
				DEBUG_LOG.debug("Getting response from Yandex server");
				final Response response = Request.Post(urlString.toString().replace(" ", "%20")).viaProxy(proxy).execute();
				DEBUG_LOG.debug("Yandex response was recieved");
				translations[count] = getTranslation(response.returnContent().asString());
				count++;
			}
			toTranslate.setTranslation(translations);
		}
		catch (final IOException e)
		{
			JOptionPane.showMessageDialog(null, "An error occured while sending the request to the Yandex.Translate server!\nPlease try again",
					"Error", JOptionPane.ERROR_MESSAGE);
			LOG.error("Error in sending request to Yandex.Translate", e);
		}
		catch (final NullPointerException en)
		{
			JOptionPane.showMessageDialog(null, "An error occured while sending the request to the Yandex.Translate server!\nPlease try again",
					"Error", JOptionPane.ERROR_MESSAGE);
			LOG.error("Error in getting content from Yandex.Translate response", en);
		}
	}

	private static String getTranslation(final String response)
	{
		final JsonParser parser = new JsonParser();
		final JsonObject json = parser.parse(response).getAsJsonObject();

		DEBUG_LOG.debug("Trying to get content from Yandex respose");

		return json.get("text").getAsString().trim();
	}

	private static String getLanguage()
	{
		return langFrom.substring(0, 2) + "-" + langTo.substring(0, 2);
	}

	public static void setLanguages(final String from, final String to)
	{
		langFrom = from;
		langTo = to;
	}

	public static String getLangFrom()
	{
		return langFrom;
	}

	public static String getLangTo()
	{
		return langTo;
	}

}
