package main;
import search.SearchDemo;
import search.SearchDemo.Distance;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

/**
 * Created by workshop on 9/18/2015.
 */
public class SoundEffectDemo extends JFrame implements ActionListener{

    /**
	 * 
	 */
	private static final long serialVersionUID = -486421616503270824L;
	
	
	JPanel contentPane;
    JButton openButton, searchButton, queryButton;
    JFileChooser fileChooser;

    File queryAudio = null;
    int resultSize = 20;
    /**
     * If need, please replace the 'querySet' with specific path of test set of audio files in your PC.
     */
    String querySet = "data/input/";
    /**
     * Please Replace the 'basePath' with specific path of train set of audio files in your PC.
     */
    String basePath = "data/input/train/";


    JButton[] resultButton = new JButton[resultSize];
    JLabel [] resultLabels = new JLabel[resultSize];
    ArrayList<String> resultFiles = new ArrayList<String>();

    // Constructor
    public SoundEffectDemo() {
        // Pre-load all the sound files
        queryAudio = null;
        SoundEffect.volume = SoundEffect.Volume.LOW;  // un-mute

        // Set up UI components;
        openButton = new JButton("Select an audio clip...");
        openButton.addActionListener(this);

        String tempName = "";

        queryButton = new JButton("Current Audio:"+tempName);
        queryButton.addActionListener(this);

        searchButton = new JButton("Search");
        searchButton.addActionListener(this);

        SimpleSoundCapture ssc = new SimpleSoundCapture();
		ssc.open();
        
        JPanel queryPanel = new JPanel();
        queryPanel.add(openButton);
        queryPanel.add(queryButton);
        queryPanel.add(searchButton);
        queryPanel.add(ssc);
        
        ssc.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
               if (evt.getPropertyName().equals(SimpleSoundCapture.COMBO_CHANGED)) {
                  String innerValue = evt.getNewValue().toString();
                  if (innerValue.equals(SimpleSoundCapture.RECORD_START)) {
                	  queryAudio = null;
                  } else if (innerValue.equals(SimpleSoundCapture.RECORD_STOP)) {
                	  queryAudio = new File(SimpleSoundCapture.FILE_CAPTURED_NAME);
                	  queryButton.setText("recorded audio");
                  }
               }
            }
         });

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new GridLayout(0, 4, 60, 60));

        for (int i = 0; i < resultLabels.length; i ++){
            resultLabels[i] = new JLabel();

            resultButton[i] = new JButton(resultLabels[i].getText());

            resultButton[i].addActionListener(this);

            resultButton[i].setVisible(false);
            resultPanel.add(resultLabels[i]);
            resultPanel.add(resultButton[i]);
        }


        resultPanel.setBorder(BorderFactory.createEmptyBorder(30,16,10,16));

        contentPane = (JPanel)this.getContentPane();
        setSize(800,900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPane.add(queryPanel, BorderLayout.PAGE_START);
        contentPane.add(resultPanel, BorderLayout.CENTER);

        contentPane.setVisible(true);
        setVisible(true);

    }

    public void actionPerformed(ActionEvent e){
        if (e.getSource() == openButton){
            if (fileChooser == null) {
                fileChooser = new JFileChooser(querySet);

                fileChooser.addChoosableFileFilter(new AudioFilter());
                fileChooser.setAcceptAllFileFilterUsed(false);
            }
            int returnVal = fileChooser.showOpenDialog(SoundEffectDemo.this);

            if (returnVal == JFileChooser.APPROVE_OPTION){
                queryAudio = fileChooser.getSelectedFile();
            }

            fileChooser.setSelectedFile(null);

            queryButton.setText(queryAudio.getName());

            fileChooser.setSelectedFile(null);

        }else if (e.getSource() == searchButton){
        	if (queryAudio == null) {
        		System.out.println("SoundEffectDemo: actionPerformed() : query audio is null");
        		return;
        	}
            SearchDemo searchDemo = new SearchDemo();
            resultFiles = searchDemo.resultListOfMfcc(queryAudio.getAbsolutePath(), true, Distance.CITYBLOCK);

            for (int i = 0; i < resultFiles.size(); i ++){
                resultLabels[i].setText(resultFiles.get(i));
                resultButton[i].setText(resultFiles.get(i));
                resultButton[i].setVisible(true);
            }

        }else if (e.getSource() == queryButton){
        	if (queryAudio == null) {
        		System.out.println("SoundEffectDemo: actionPerformed() : query audio is null");
        		return;
        	}
            new SoundEffect(queryAudio.getAbsolutePath()).play();
        }else {
            for (int i = 0; i < resultSize; i ++){
                if (e.getSource() == resultButton[i]){
                    String filePath = basePath+resultFiles.get(i);
                    new SoundEffect(filePath).play();
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        new SoundEffectDemo();
    }
}
