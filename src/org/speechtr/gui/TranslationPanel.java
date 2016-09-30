package org.speechtr.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.apache.http.conn.HttpHostConnectException;
import org.apache.log4j.Logger;
import org.speechtr.data.ProjectProperties.NoSuchPropertyException;
import org.speechtr.data.RecognizedVoiceObject;
import org.speechtr.translation.Translator;

public class TranslationPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(TranslationPanel.class);
	private static final Logger DEBUG_LOG = Logger.getLogger("debuger");

	private final JButton btnSaveChanges;
	private final JButton btnEdit;
	private final JTextArea editorPane;
	private final JTextArea editorPane1;

	/**
	 * Create the panel.
	 */
	public TranslationPanel()
	{

		btnSaveChanges = new JButton("Save changes");
		btnSaveChanges.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				if (!SpeechTranslatorGui.getStepLabel().isVisible())
				{
					SpeechTranslatorGui.getStepLabel().setVisible(true);
				}
				SpeechTranslatorGui.getProgressBar().setVisible(true);
				DEBUG_LOG.debug("Changes were saved");
				final RecognizedVoiceObject recVoiceObj = new RecognizedVoiceObject(new String[] { editorPane.getText() });
				try
				{
					Translator.translate(recVoiceObj);
				}
				catch (final HttpHostConnectException e)
				{
					LOG.error(e);
					JOptionPane.showMessageDialog(null, "Cannot connect to the Service.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				catch (final UnknownHostException e)
				{
					LOG.error(e);
					JOptionPane.showMessageDialog(null,
							"Unknown host " + e.getMessage()
									+ ".\nMake sure that you have Internet connection or you set correct proxy in config.properties file.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				catch (final NoSuchPropertyException e)
				{
					LOG.error(e);
					JOptionPane.showMessageDialog(null, e.getMessage(), "System error", JOptionPane.ERROR_MESSAGE);
				}
				finally
				{
					SpeechTranslatorGui.getStepLabel().setVisible(false);
					SpeechTranslatorGui.getProgressBar().setVisible(false);
				}

				editorPane1.setText(recVoiceObj.getTranslation()[0]);
				editorPane.setEditable(false);
				btnEdit.setEnabled(true);
				btnSaveChanges.setEnabled(false);
			}
		});
		btnSaveChanges.setEnabled(false);

		btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				DEBUG_LOG.debug("Edit the translation");
				editorPane.setEditable(true);
				btnSaveChanges.setEnabled(true);
				btnEdit.setEnabled(false);
			}
		});

		editorPane = new JTextArea();
		editorPane.setLineWrap(true);
		editorPane.setFont(new Font("Dialog", Font.PLAIN, 12));

		editorPane1 = new JTextArea();
		editorPane1.setLineWrap(true);
		editorPane1.setFont(new Font("Dialog", Font.PLAIN, 12));

		editorPane.setEditable(false);
		editorPane1.setEditable(false);

		final GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(Alignment.LEADING,
										groupLayout.createSequentialGroup().addContainerGap().addComponent(editorPane1, GroupLayout.DEFAULT_SIZE, 434,
												Short.MAX_VALUE))
								.addGroup(Alignment.LEADING,
										groupLayout.createSequentialGroup().addContainerGap().addComponent(editorPane,
												GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE))
								.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup().addGap(16).addComponent(btnEdit)
										.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(btnSaveChanges)))
						.addContainerGap()));
		groupLayout
				.setVerticalGroup(
						groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(
										groupLayout.createSequentialGroup().addContainerGap()
												.addComponent(editorPane, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(btnEdit)
														.addComponent(btnSaveChanges))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(editorPane1, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
												.addContainerGap(30, Short.MAX_VALUE)));
		setLayout(groupLayout);

	}

	public void setText(final String transcription, final String translation)
	{
		if (Translator.getLangFrom().contains("ko-kr"))
		{
			editorPane.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
		}
		if (Translator.getLangTo().contains("ko-kr"))
		{
			editorPane1.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
		}
		editorPane.setText(transcription);
		editorPane1.setText(translation);
		DEBUG_LOG.debug("Recognized and translated text was set");
	}

	public String getSource()
	{
		return editorPane.getText();
	}

	public String getTranslation()
	{
		return editorPane1.getText();
	}

	public String getAllText()
	{
		return "Source:\n" + editorPane.getText() + "\nTranslation:\n" + editorPane1.getText();
	}
}
