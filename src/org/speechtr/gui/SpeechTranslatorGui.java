package org.speechtr.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
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
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.http.conn.HttpHostConnectException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.speechtr.convertation.SpeechRecognizer;
import org.speechtr.convertation.SpeechRecognizer.NullGoogleResponseException;
import org.speechtr.convertation.SpeechRecognizer.Server502ResponseException;
import org.speechtr.data.Languages;
import org.speechtr.data.ProjectProperties;
import org.speechtr.data.ProjectProperties.NoSuchPropertyException;
import org.speechtr.data.RecognizedVoiceObject;
import org.speechtr.data.WaveAudioObject;
import org.speechtr.translation.Translator;

public class SpeechTranslatorGui
{
	private static JFrame frame;
	private static JTextField textField;
	private static JComboBox<?> comboBox;
	private static JComboBox<?> comboBox1;

	private static JLabel stepLabel;
	private static JProgressBar progressBar;

	private static String recentPath;

	private static Clip clip;

	private static final Logger LOG = Logger.getRootLogger();
	private static final Logger DEBUG_LOG = Logger.getLogger("debuger");

	private static Languages languages;
	{
		try
		{
			languages = new Languages();
			clip = AudioSystem.getClip();
		}
		catch (FileNotFoundException | URISyntaxException e)
		{
			LOG.error("File langs.properties was not found", e);
			JOptionPane.showMessageDialog(frame, "File langs.properties was not found", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (final IOException e)
		{
			LOG.error("Problem with opening langs.properties file", e);
			JOptionPane.showMessageDialog(frame, "Problem with opening langs.properties file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (final LineUnavailableException e)
		{
			LOG.error("Problem in creating Clip object", e);
			JOptionPane.showMessageDialog(frame, "Problem occured while launching the application", "Launching error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args)
	{
		DOMConfigurator.configure(Languages.class.getClassLoader().getResource("log4j.xml").getPath().replace("!", ""));
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					@SuppressWarnings("unused")
					final SpeechTranslatorGui window = new SpeechTranslatorGui();
					SpeechTranslatorGui.frame.setVisible(true);
					DEBUG_LOG.debug("GUI object was created");
				}
				catch (final Exception e)
				{
					LOG.error("Error occured in application launching", e);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SpeechTranslatorGui()
	{
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void initialize()
	{
		frame = new JFrame();
		frame.setBounds(100, 100, 583, 436);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Speech Translator");

		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setEnabled(false);

		final JPanel panel = new JPanel();

		final JPanel panel1 = new JPanel();

		final JPanel panel2 = new JPanel();

		final JSeparator separator = new JSeparator();
		final GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING,
				groupLayout.createSequentialGroup().addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(separator, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
								.addComponent(panel1, Alignment.LEADING, 0, 0, Short.MAX_VALUE).addGroup(Alignment.LEADING,
										groupLayout.createParallelGroup(Alignment.TRAILING, false)
												.addComponent(panel2, Alignment.LEADING, 0, 0, Short.MAX_VALUE).addComponent(panel, Alignment.LEADING,
														GroupLayout.PREFERRED_SIZE, 557, Short.MAX_VALUE)))
						.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addContainerGap()
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, 4, GroupLayout.PREFERRED_SIZE).addGap(8)
						.addComponent(panel1, GroupLayout.PREFERRED_SIZE, 279, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(panel2, GroupLayout.PREFERRED_SIZE, 44, Short.MAX_VALUE).addContainerGap()));

		final JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				DEBUG_LOG.debug("Save button was clicked");
				final JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					final File file = new File(fileChooser.getSelectedFile().getAbsolutePath() + ".txt");
					try (FileOutputStream fos = new FileOutputStream(file); OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8"))
					{
						if (!file.exists())
							file.createNewFile();
						osw.write(((TranslationPanel) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex())).getAllText());
						DEBUG_LOG.debug("New file was saved to " + file.getAbsolutePath());
					}
					catch (final IOException e1)
					{
						LOG.error("Error occured while creating a file", e1);
						JOptionPane.showMessageDialog(frame, "An error occured while saving a file.\nFile was not saved!", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		btnSave.setEnabled(false);

		final JButton btnCopyToClipboard = new JButton("Copy to clipboard");
		btnCopyToClipboard.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				DEBUG_LOG.debug("Copy button was clicked");
				final String text = ((TranslationPanel) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex())).getAllText();
				final StringSelection stringSelection = new StringSelection(text);
				final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, stringSelection);
			}
		});
		btnCopyToClipboard.setEnabled(false);

		progressBar = new JProgressBar();
		progressBar.setVisible(false);

		stepLabel = new JLabel("");

		final GroupLayout glpanel2 = new GroupLayout(panel2);
		glpanel2.setHorizontalGroup(glpanel2.createParallelGroup(Alignment.TRAILING).addGroup(glpanel2.createSequentialGroup().addGap(2)
				.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
				.addGroup(glpanel2.createParallelGroup(Alignment.TRAILING)
						.addGroup(glpanel2.createSequentialGroup().addPreferredGap(ComponentPlacement.RELATED).addComponent(stepLabel)
								.addPreferredGap(ComponentPlacement.RELATED, 425, Short.MAX_VALUE))
						.addGroup(glpanel2.createSequentialGroup().addPreferredGap(ComponentPlacement.RELATED).addComponent(btnCopyToClipboard)
								.addPreferredGap(ComponentPlacement.RELATED)))
				.addComponent(btnSave).addContainerGap()));
		glpanel2.setVerticalGroup(glpanel2.createParallelGroup(Alignment.LEADING)
				.addGroup(glpanel2.createSequentialGroup()
						.addGroup(glpanel2.createParallelGroup(Alignment.LEADING)
								.addGroup(glpanel2.createSequentialGroup().addContainerGap()
										.addGroup(glpanel2.createParallelGroup(Alignment.BASELINE).addComponent(btnSave).addComponent(stepLabel)
												.addComponent(btnCopyToClipboard)))
								.addGroup(glpanel2.createSequentialGroup().addGap(15).addComponent(progressBar, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap(10, Short.MAX_VALUE)));
		panel2.setLayout(glpanel2);

		comboBox = new JComboBox(languages.getValues());
		comboBox1 = new JComboBox(languages.getValues());
		comboBox1.setSelectedIndex(5);
		comboBox.setEnabled(false);
		comboBox1.setEnabled(false);

		final JLabel lblFrom = new JLabel("From:");
		final JLabel lblTo = new JLabel("To:");

		final JButton btnAbort = new JButton("Abort");
		btnAbort.setEnabled(false);

		final JButton btnPlay = new JButton();
		btnPlay.setIcon(new ImageIcon("icons/play16.png"));

		final JButton btnTranslate = new JButton("Translate");

		final JButton btnBrowse = new JButton("Browse");
		final DropTargetListener dropListener = new DropTargetListener()
		{
			@Override
			public void dragEnter(final DropTargetDragEvent dtde)
			{
			}

			@Override
			public void dragExit(final DropTargetEvent dte)
			{
			}

			@Override
			public void dragOver(final DropTargetDragEvent dtde)
			{
			}

			@Override
			public void drop(final DropTargetDropEvent event)
			{
				if (!btnBrowse.isEnabled())
				{
					event.rejectDrop();
					return;
				}
				event.acceptDrop(DnDConstants.ACTION_COPY);
				final Transferable transferable = event.getTransferable();
				try
				{
					final List<File> file = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
					if (!file.isEmpty())
					{
						if (clip != null || clip.isRunning())
						{
							clip.stop();
							clip.drain();
							clip.close();
						}

						cleanUp(tabbedPane, comboBox, textField, btnTranslate, btnCopyToClipboard, btnSave, btnPlay);
						comboBox1.setSelectedIndex(5);
						comboBox1.setEnabled(false);

						SpeechRecognizer.setRecentlyRecognized(null);

						DEBUG_LOG.debug("Opened file: " + file.get(0).getAbsolutePath());
						if (!file.get(0).getName().endsWith(".wav"))
						{
							DEBUG_LOG.debug("Opened file is not a WAVE file");
							JOptionPane.showMessageDialog(frame, "Only WAVE files are available", "Wrong file format", JOptionPane.ERROR_MESSAGE);
							return;
						}
						recentPath = file.get(0).getParentFile().getAbsolutePath();
						textField.setText(file.get(0).getAbsolutePath());
						setAllEnabled(true, comboBox, comboBox1, btnTranslate, btnPlay);
					}
				}
				catch (final UnsupportedFlavorException e)
				{
					LOG.error("Unsupported flavor!", e);
					JOptionPane.showMessageDialog(frame, "Unsupported flavor!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				catch (final IOException e)
				{
					LOG.error("Error in getting file", e);
					JOptionPane.showMessageDialog(frame, "Error in getting file", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				event.dropComplete(true);
			}

			@Override
			public void dropActionChanged(final DropTargetDragEvent dtde)
			{
			}
		};
		new DropTarget(btnBrowse, dropListener);

		btnTranslate.setEnabled(false);
		btnTranslate.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				DEBUG_LOG.debug("Translate button was clicked");
				final CancelableThread translate = new CancelableThread("Translator")
				{
					@Override
					public void cancel()
					{
						this.canceled = true;
					}

					@Override
					public void run()
					{
						DEBUG_LOG.debug("Run translation thread");
						btnAbort.setEnabled(true);
						setAllEnabled(false, comboBox, comboBox1, btnTranslate, btnBrowse);
						progressBar.setIndeterminate(true);
						progressBar.setVisible(true);

						if (!stepLabel.isVisible())
						{
							stepLabel.setVisible(true);
						}
						stepLabel.setText("Preparing data for the speech recognition...");

						try
						{
							final WaveAudioObject audio = new WaveAudioObject(textField.getText());
							if (audio.getAudio() == null)
							{
								return;
							}
							RecognizedVoiceObject convertedText;
							SpeechRecognizer.setLanguage(languages.valueOf((String) comboBox.getSelectedItem()));
							DEBUG_LOG.debug("Recognition language set to " + SpeechRecognizer.getLanguage());

							if (SpeechRecognizer.getRecentlyRecognized() == null || SpeechRecognizer.langWasChanged())
							{
								DEBUG_LOG.debug("Start of recognition process");
								convertedText = recognize(audio);
								if (canceled)
								{
									DEBUG_LOG.debug("Thread was canceled");
									return;
								}
								SpeechRecognizer.setRecentlyRecognized(convertedText);
								SpeechRecognizer.setOldLanguage(languages.valueOf((String) comboBox.getSelectedItem()));
								if (convertedText == null)
								{
									DEBUG_LOG.debug("RecognizedVoiceObject is null");
									cleanUp(btnAbort, stepLabel, progressBar);
									setAllEnabled(true, comboBox, comboBox1, btnTranslate, btnBrowse);
									return;
								}
							}
							else
							{
								DEBUG_LOG.debug("Old RecognizedVoiceObject object was used");
								convertedText = SpeechRecognizer.getRecentlyRecognized();
							}

							if (canceled)
							{
								DEBUG_LOG.debug("Thread was canceled");
								return;
							}

							stepLabel.setText("Preparing data for the translation...");
							DEBUG_LOG.debug("Start of translation process");
							Translator.setLanguages(languages.valueOf((String) comboBox.getSelectedItem()),
									languages.valueOf((String) comboBox1.getSelectedItem()));
							DEBUG_LOG.debug("Translation languages was set as " + Translator.getLangFrom() + "->" + Translator.getLangTo());
							Translator.translate(convertedText);

							if (convertedText.getTranslation() == null || convertedText.getTranslation().length == 0)
							{
								DEBUG_LOG.debug("Translation process gone wrong. Translated text is null");
								btnAbort.setEnabled(false);
								stepLabel.setVisible(false);
								progressBar.setVisible(false);
								setAllEnabled(true, comboBox, comboBox1, btnTranslate, btnBrowse);
								return;
							}

							if (canceled)
							{
								DEBUG_LOG.debug("Thread was canceled");
								return;
							}

							stepLabel.setText("");
							tabbedPane.setEnabled(true);
							tabbedPane.removeAll();

							for (int i = 0; i < convertedText.getTranslation().length; i++)
							{
								if (convertedText.getTranslation()[i] == null)
								{
									continue;
								}

								tabbedPane.addTab("Translation " + (i + 1), new TranslationPanel());
								int j = i;
								while (convertedText.getTranscription()[j] == null)
								{
									j++;
								}

								((TranslationPanel) tabbedPane.getComponentAt(i)).setText(convertedText.getTranscription()[j],
										convertedText.getTranslation()[i]);
							}

						}
						catch (final HttpHostConnectException e)
						{
							JOptionPane.showMessageDialog(frame, "Cannot connect to the Service.\n" + e.getMessage(), "System error",
									JOptionPane.ERROR_MESSAGE);
							LOG.error(e);
						}
						catch (final UnknownHostException e)
						{
							JOptionPane.showMessageDialog(frame,
									"Unknown host " + e.getMessage()
											+ ".\nMake sure that you have Internet connection or you set correct proxy in config.properties file.",
									"System error", JOptionPane.ERROR_MESSAGE);
							LOG.error(e);
						}
						catch (final IOException e)
						{
							JOptionPane.showMessageDialog(frame, "An error occured while sending the request to the Google server", "System error",
									JOptionPane.ERROR_MESSAGE);
							LOG.error("Error in sending request to Google server", e);
						}
						catch (final IllegalStateException e)
						{
							JOptionPane.showMessageDialog(frame, "An error occured while getting a content from Google response", "System error",
									JOptionPane.ERROR_MESSAGE);
							LOG.error("Error in getting content from Google response", e);
						}
						catch (final Server502ResponseException e)
						{
							if (canceled)
							{
								DEBUG_LOG.debug("Thread was canceled");
								return;
							}
							LOG.error("502 Bad Gateway response from Google", e);
							JOptionPane.showMessageDialog(frame, "Server returned 502 Bad Gateway response\n" + "Server message:\n" + e.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}
						catch (final NullGoogleResponseException e)
						{
							if (canceled)
							{
								DEBUG_LOG.debug("Thread was canceled");
								return;
							}
							LOG.error("Google Servise returned empty response", e);
							JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
						catch (final NoSuchPropertyException e)
						{
							if (canceled)
							{
								DEBUG_LOG.debug("Thread was canceled");
								return;
							}
							LOG.error(e);
							JOptionPane.showMessageDialog(frame, e.getMessage(), "System error", JOptionPane.ERROR_MESSAGE);
						}
						catch (final UnsupportedAudioFileException e)
						{
							if (canceled)
							{
								DEBUG_LOG.debug("Thread was canceled");
								return;
							}
							LOG.error("Audio file format is mot supported", e);
							JOptionPane.showMessageDialog(frame,
									"Audio file format is not supported.\nOnly WAVE audio files in PCM or ULAW (ALAW) encoding is allowed",
									"System error", JOptionPane.ERROR_MESSAGE);
						}
						setAllEnabled(true, comboBox, comboBox1, btnTranslate, btnBrowse, btnSave, btnCopyToClipboard);
						cleanUp(btnAbort);
						cleanUp(progressBar, stepLabel);
					}

					private RecognizedVoiceObject recognize(final WaveAudioObject audio) throws IOException, IllegalStateException,
							Server502ResponseException, NullGoogleResponseException, NoSuchPropertyException, UnsupportedAudioFileException
					{
						DEBUG_LOG.debug("recognize() method started");
						if (isLongAudio(audio))
						{
							final int conf = JOptionPane.showConfirmDialog(frame,
									"The audio file is too long for Google Speech recognition. It has to be less than or equals 10 seconds.\n Would you like to split it?",
									"Long audio file", JOptionPane.YES_NO_OPTION);
							if (conf != 0)
							{
								setAllEnabled(true, comboBox, comboBox1, btnTranslate, btnBrowse);
								cleanUp(progressBar, stepLabel, btnAbort);
								return null;
							}
							else
							{
								if (clip.isOpen())
								{
									clip.close();
								}

								List<Integer> splitIndexes;
								if ((splitIndexes = getSplitIndexes()) == null)
								{
									DEBUG_LOG.debug("No split indexes");
									return null;
								}
								final WaveAudioObject[] audioObjects = splitAudio(audio, splitIndexes);
								DEBUG_LOG.debug("Audio file was splited into " + (splitIndexes.size() + 1) + " parts");
								final RecognizedVoiceObject[] recognizedObjects = new RecognizedVoiceObject[audioObjects.length];

								for (int i = 0; i < audioObjects.length; i++)
								{
									recognizedObjects[i] = SpeechRecognizer.getConvertedText(audioObjects[i]);
								}
								return RecognizedVoiceObject.concat(recognizedObjects);
							}
						}
						else
						{
							return SpeechRecognizer.getConvertedText(audio);
						}
					}

					private ArrayList<Integer> getSplitIndexes() throws IOException, UnsupportedAudioFileException
					{
						final SplitPanel splitPanel = new SplitPanel(textField.getText());
						JOptionPane.showOptionDialog(frame, splitPanel, "Audio splitting", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
								new Object[] {}, null);

						if (splitPanel.getClip().isOpen())
						{
							splitPanel.getClip().close();
						}

						if (splitPanel.getSplitIndexes().isEmpty())
						{
							JOptionPane.showMessageDialog(frame,
									"The audio file has to be splited to fit Google Speech Recognition requirements!\nAudio file duration has to be less than or equals 10 seconds",
									"Audio has to be splited", JOptionPane.ERROR_MESSAGE);
							return null;
						}
						return (ArrayList<Integer>) splitPanel.getSplitIndexes();
					}

					private WaveAudioObject[] splitAudio(final WaveAudioObject audio, final List<Integer> splitIndexes) throws IOException
					{
						if (splitIndexes == null)
						{
							return null;
						}

						final WaveAudioObject[] audioObjects = new WaveAudioObject[splitIndexes.size() + 1];
						final ByteArrayInputStream baisSource = new ByteArrayInputStream(audio.getAudioBytes());
						final int allCount = baisSource.available();
						int allRead = 0;

						for (int i = 0; i < audioObjects.length; i++)
						{
							if (i <= splitIndexes.size() - 1)
							{
								final byte[] partArray = new byte[splitIndexes.get(i) - allRead];
								allRead += baisSource.read(partArray);

								audioObjects[i] = new WaveAudioObject(new AudioInputStream(new ByteArrayInputStream(partArray),
										new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false),
										partArray.length / audio.getAudio().getFormat().getFrameSize()));
							}
							else
							{
								final byte[] remainingBytesArray = new byte[allCount - allRead];
								final int read = baisSource.read(remainingBytesArray);
								if (read < 0)
								{
									throw new IOException();
								}

								audioObjects[i] = new WaveAudioObject(new AudioInputStream(new ByteArrayInputStream(remainingBytesArray),
										new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false),
										remainingBytesArray.length / audio.getAudio().getFormat().getFrameSize()));
							}
						}
						return audioObjects;
					}

					private boolean isLongAudio(final WaveAudioObject audio) throws NumberFormatException, NoSuchPropertyException
					{
						final long frames = audio.getAudio().getFrameLength();
						final double duration = ((double) frames) / audio.getFormat().getFrameRate();
						if (duration > Double.parseDouble(ProjectProperties.getProperty("gl.maxduration")))
						{
							return true;
						}
						return false;
					}

				};
				translate.start();

			}
		});

		btnAbort.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				DEBUG_LOG.debug("Abort Button was clicked");
				final CancelableThread translator = (CancelableThread) getThreadByName("Translator");
				if (translator.isAlive())
				{
					translator.cancel();
				}
				btnAbort.setEnabled(false);
				stepLabel.setVisible(false);
				progressBar.setVisible(false);
				setAllEnabled(true, comboBox, comboBox1, btnTranslate, btnBrowse);
				SpeechRecognizer.setRecentlyRecognized(null);
			}
		});

		final JLabel arrowLabel = new JLabel();
		arrowLabel.setIcon(new ImageIcon("icons/arrow.png"));

		final GroupLayout glpanel1 = new GroupLayout(panel1);
		glpanel1.setHorizontalGroup(glpanel1.createParallelGroup(Alignment.LEADING)
				.addGroup(glpanel1.createSequentialGroup().addContainerGap().addComponent(lblFrom).addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(arrowLabel).addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblTo).addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED, 24, Short.MAX_VALUE).addComponent(btnAbort)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnTranslate).addContainerGap())
				.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE));
		glpanel1.setVerticalGroup(
				glpanel1.createParallelGroup(Alignment.LEADING)
						.addGroup(
								glpanel1.createSequentialGroup()
										.addGroup(glpanel1.createParallelGroup(Alignment.BASELINE).addComponent(lblFrom)
												.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(btnTranslate).addComponent(lblTo)
												.addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
												.addComponent(btnAbort).addComponent(arrowLabel))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)));

		panel1.setLayout(glpanel1);

		textField = new JTextField();
		textField.setColumns(10);

		btnPlay.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				DEBUG_LOG.debug("Play button was clicked");
				try
				{
					final WaveAudioObject audio = new WaveAudioObject(textField.getText());
					if (audio.getAudio() == null)
					{
						DEBUG_LOG.debug("Cannot play audio file");
						return;
					}
					clip.open(audio.getAudio());
					btnPlay.setEnabled(false);

					clip.addLineListener(new LineListener()
					{
						@Override
						public void update(final LineEvent event)
						{
							if (event.getType() == LineEvent.Type.STOP)
							{
								DEBUG_LOG.debug("EventType=STOP close Clip object");
								clip.close();
								btnPlay.setEnabled(true);
							}
						}
					});

					clip.start();
				}
				catch (LineUnavailableException | IOException e1)
				{
					LOG.error("Error occured while trying to play audio file", e1);
					JOptionPane.showMessageDialog(frame, "Error while playing audio file", "Error", JOptionPane.ERROR_MESSAGE);
				}
				catch (final UnsupportedAudioFileException e2)
				{
					LOG.error("Audio file format is mot supported", e2);
					JOptionPane.showMessageDialog(frame,
							"Audio file format is not supported.\nOnly WAVE audio files in PCM or ULAW (ALAW) encoding is allowed", "System error",
							JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		btnPlay.setIcon(new ImageIcon("icons/play16.png"));
		btnPlay.setEnabled(false);

		btnBrowse.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				DEBUG_LOG.debug("Browse button was clicked");
				final JFileChooser fileChooser = new JFileChooser((recentPath == null) ? System.getProperty("user.home") : recentPath);
				final FileFilter filter = new FileNameExtensionFilter("WAVE file", "wav");
				fileChooser.setFileFilter(filter);
				if (clip != null || clip.isRunning())
				{
					clip.stop();
					clip.drain();
					clip.close();
				}

				cleanUp(tabbedPane, comboBox, textField, btnTranslate, btnCopyToClipboard, btnSave, btnPlay);
				comboBox1.setSelectedIndex(5);
				comboBox1.setEnabled(false);

				SpeechRecognizer.setRecentlyRecognized(null);

				final int state = fileChooser.showOpenDialog(null);
				if (state == JFileChooser.APPROVE_OPTION)
				{
					final File opened = fileChooser.getSelectedFile();
					DEBUG_LOG.debug("Opened file: " + opened.getAbsolutePath());
					if (!opened.getName().endsWith(".wav"))
					{
						DEBUG_LOG.debug("Opened file is not a WAVE file");
						JOptionPane.showMessageDialog(frame, "Only WAVE files are available", "Wrong file format", JOptionPane.ERROR_MESSAGE);
						return;
					}
					recentPath = opened.getParentFile().getAbsolutePath();
					textField.setText(opened.getPath());
					setAllEnabled(true, comboBox, comboBox1, btnTranslate, btnPlay);
				}
			}
		});

		final GroupLayout glpanel = new GroupLayout(panel);
		glpanel.setHorizontalGroup(glpanel.createParallelGroup(Alignment.LEADING)
				.addGroup(glpanel.createSequentialGroup().addContainerGap()
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, 429, GroupLayout.PREFERRED_SIZE).addGap(3)
						.addComponent(btnPlay, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE).addGap(2).addComponent(btnBrowse)
						.addContainerGap(19, Short.MAX_VALUE)));
		glpanel.setVerticalGroup(glpanel.createParallelGroup(Alignment.LEADING)
				.addGroup(glpanel.createSequentialGroup().addContainerGap().addGroup(glpanel.createParallelGroup(Alignment.LEADING)
						.addGroup(glpanel.createSequentialGroup().addComponent(btnPlay, GroupLayout.PREFERRED_SIZE, 23, Short.MAX_VALUE)
								.addContainerGap())
						.addGroup(glpanel.createSequentialGroup()
								.addGroup(glpanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(textField, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addComponent(btnBrowse,
												GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addGap(9)))));
		panel.setLayout(glpanel);
		frame.getContentPane().setLayout(groupLayout);
	}

	@SuppressWarnings("rawtypes")
	private static void cleanUp(final Component... components)
	{
		for (final Component component : components)
		{
			if (component instanceof JTabbedPane)
			{
				((JTabbedPane) component).removeAll();
			}
			if (component instanceof JTextField)
			{
				((JTextField) component).setText("");
			}
			if (component instanceof JComboBox)
			{
				((JComboBox) component).setSelectedIndex(0);
				component.setEnabled(false);
			}
			if (component instanceof JButton)
			{
				component.setEnabled(false);
			}
			if (component instanceof JProgressBar)
			{
				component.setVisible(false);
			}
			if (component instanceof JLabel)
			{
				component.setVisible(false);
			}
		}
	}

	public static JLabel getStepLabel()
	{
		return stepLabel;
	}

	public static JProgressBar getProgressBar()
	{
		return progressBar;
	}

	private static Thread getThreadByName(final String name)
	{
		final Set<Thread> threads = Thread.getAllStackTraces().keySet();
		for (final Thread thread : threads)
		{
			if (thread.getName().equals(name))
			{
				return thread;
			}
		}
		return null;
	}

	private static void setAllEnabled(final boolean value, final Component... components)
	{
		for (final Component component : components)
		{
			component.setEnabled(value);
		}
	}

	public static String getLangFrom()
	{
		return (String) comboBox.getSelectedItem();
	}

	public static String getLangTo()
	{
		return (String) comboBox1.getSelectedItem();
	}

	static class CancelableThread extends Thread
	{
		protected volatile boolean canceled = false;

		public CancelableThread(final String name)
		{
			super(name);
		}

		public void cancel()
		{
			this.setCanceled(true);
		}

		@Override
		public void run()
		{
			super.run();
		}

		public boolean isCanceled()
		{
			return canceled;
		}

		public void setCanceled(final boolean canceled)
		{
			this.canceled = canceled;
		}
	}
}