import static javafx.concurrent.Worker.State.FAILED;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.omg.CORBA_2_3.portable.InputStream;

import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import javafx.scene.web.*;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
/******************************************************************************
 * 
 * Name:		Jason Scharff
 * Block:		A
 * Date:		9/20/14
 * 
 *  Program #1: Web Browser
 *  Description:
 *     This program is the front-end of a browser using the Java FX web engine. The features
 *     in this web browser include the ability to send the current web page via the service yo 
 *    to your smartphone, a style of branched history such that all clicked hyperlinks show up
 *    as a child of the website it was clicked from, a method of activating the url bar in the same
 *    manor as the mac program Alfred (activated in this case by clicking control s), and back and forward
 *    buttons, and if you type in @ then a twitter handle it redirects to their twitter account.
 *     With the Java FX engine there are countless text rendering errors, but as that is part of the engine
 *    it can't be fixed without using a separate engine
 * 
 ******************************************************************************/

public class browserWindow extends JFrame
							 implements KeyListener		
{	
	//Class scopes
	private static JEditorPane editorPane;
	private final static String homePage = "http://www.google.com/";
	private static JButton backButton = new JButton("Back");
	private static JButton forwardButton = new JButton("Forward");
	private static JButton yoButton = new JButton("Send to Phone");
	private static JButton historyButton = new JButton ("History");
	private static JFrame yoFrame = new JFrame();
	private static JTextField urlBar = new JTextField (homePage, 45);
	private static JTextField urlBarPopUp;
	private static JFrame urlFrame;
	private static JFrame historyFrame;
	private static JPanel navPanel = new JPanel();
	static JTextField yoField = new JTextField ("Enter your yo username", 45);
	private static ArrayList <historyObject> linearHistory = new ArrayList<historyObject>();
	private static int placeInLinear = 0;
	private static String lastMousedOver;
	private static treeNode currentNode =  new treeNode(null, null);
	//JFX
	 private final JFXPanel jfxPanel = new JFXPanel();
	   private static WebEngine engine; 
	
	
	   
		
	public static void main(String[] args) 
	{	
		yoFrame.setSize(570, 55);
		yoFrame.setResizable(false);
		yoFrame.setLocationRelativeTo(null);
		yoFrame.setVisible(false);
		
		SwingUtilities.invokeLater(new Runnable() {

            public void run() {
            	browserWindow browser = new browserWindow();
                browser.setVisible(true);
                browser.loadURL(homePage);
                linearHistory.add( new historyObject (true, homePage));
                placeInLinear = 0;
            }
        });
	}
	
	
	 
	/**
	 * This method is called whenever a key is pressed. It simply determines if the user
	 * clicked the key combination Control S and then if they do activates the window to
	 * to enter the URl bar in a manor similar to Alfred.
	 */
	public void keyPressed(KeyEvent e)					
	{
		if(e.getKeyCode() == KeyEvent.VK_S && e.isControlDown() == true)
		{
			urlFrame  = new JFrame();
			urlFrame.setSize(570, 55);
			urlFrame.setResizable(false);
			urlFrame.setLocationRelativeTo(null);
			urlFrame.setVisible(true);
			
			
			urlBarPopUp = new JTextField("type in a URL", 45);
			JPanel panel = new JPanel();
			panel.add(urlBarPopUp);
			urlBarPopUp.addActionListener(new NavActionPopUp());
			urlFrame.add(panel, BorderLayout.CENTER);
			
		}
		
	}
	

	/**
	 * Empty method required for the interface KeyListener
	 */
	public void keyTyped(KeyEvent e)					
	{
		
	}
	
	/**
	 * Empty method required for the interface KeyListener
	 */
	public void keyReleased(KeyEvent e)					
	{

	}
	
	/******************************************************************************
	* The following class is used is an action listener for when the forward button
	* is pressed.It only provides a method to deal with the action of pressing the button.
	*	
	 ******************************************************************************/
	public static class forwardAction implements ActionListener
	{
		/**
		 * This method moves the current page forward along the
		 * linear history. After moving forward in the linear history list,
		 * it loads the new web page and adjusts the place in the branches history accordingly.
		 * Also, if necessary, it disables the forward button.  
		 */
		public void actionPerformed (ActionEvent e)
		{
			placeInLinear++;
			backButton.setEnabled(true);
			String urlString = linearHistory.get(placeInLinear).getUrl();
			if(linearHistory.get(placeInLinear).wasEntered() == true)
			{
				currentNode = currentNode.getRoot();
			}
			else
			{
				currentNode = currentNode.getParent().nodeWith(urlBar.getText());
				currentNode = currentNode.nodeWith(urlString);
			}
			
				loadURL(urlString);
				urlBar.setText(urlString);
			
			
			if(placeInLinear == linearHistory.size() - 1)
			{
				forwardButton.setEnabled(false);
			}
			else
			{
				System.out.println(linearHistory.size());
				System.out.println(placeInLinear);
			}

			
		}
	
		
		
	}
	
	/******************************************************************************
	* The following class is used is an action listener for when a url is entered
	* and the user clicks enter. It only has one method that hanldes the action
	* of the user pressing enter	
	 ******************************************************************************/
	private static class NavAction implements ActionListener
	{
		/**
		 * This method gets the text in the URL field and adds it to the linear history. 
		 * It then disables the forward button and loads the URL.
		 */
		public void actionPerformed (ActionEvent e)
		{
			String urlString = urlBar.getText();
			linearHistory.add(new historyObject(true, urlString));
			placeInLinear = linearHistory.size() - 1;
			forwardButton.setEnabled(false);
			loadURL(urlString);
	
			if(placeInLinear >= 1)
			{
				backButton.setEnabled(true);
			}

		}

	}
	
	/******************************************************************************
	* The following class is used is an action listener for when a url is entered
	* and the user clicks enter. It only has one method that hanldes the action
	* of the user pressing enter. The difference between this class and NavAction
	* is this one is exclusive to the pop up URL bar to handle text things.
	 ******************************************************************************/
	private static class NavActionPopUp implements ActionListener
	{
		/**
		 * This method gets the text in the URL field and adds it to the linear history. 
		 * It then disables the forward button and loads the URL.
		 */
		public void actionPerformed (ActionEvent e)
		{
			String urlString = urlBarPopUp.getText();
			urlBar.setText(urlString);
			linearHistory.add(new historyObject(true, urlString));
			placeInLinear = linearHistory.size() - 1;
			forwardButton.setEnabled(false);
			urlFrame.setVisible(false);
			loadURL(urlString);
	
			if(placeInLinear >= 1)
			{
				backButton.setEnabled(true);
			}

		}

	}
	
	
	/******************************************************************************
	* The following class is used is an action listener for when the user clicks the send to phone button.
	* It only contains one method that initializes the new window.
	 ******************************************************************************/
	private static class initYoView implements ActionListener
	{
		/**
		 * The following method handles the action of pressing the "Send to Phone" button.
		 * It simply shows the frame and adds the proper fields and action listeners for those fields
		 */
		public void actionPerformed (ActionEvent e)
		{
			yoFrame.setVisible(true);
			JPanel panel = new JPanel();
			panel.add(yoField);
			yoField.setText("Enter your yo username");
			yoField.addActionListener(new yoAction());
			urlBar.addActionListener(new NavAction());
			yoFrame.add(panel, BorderLayout.CENTER);
		}
		
	}
	
	/******************************************************************************
	* The following class is used is an action listener for when the user presses the
	* enter key when attempting to yo the current page to themselves. This method 
	* closes the frame then sends an HTTP POST request with the proper parameters to send
	* a yo.
	 ******************************************************************************/
	
	private static class yoAction implements ActionListener
	{
		/**
		 * This method simply disables the frame, then calls the method to use the Yo API.
		 */
		public void actionPerformed (ActionEvent e)
		{
			yoFrame.setVisible(false);
			sendPostRequest();
		}
		
		/**
		 * This method sends a HTTP POST Request to the proper URL with
		 * the proper parameters to send a yo to the user
		 */
		public void sendPostRequest()
		{
				
			String token = "2427be1b-8f4f-6af0-3ce5-b396b6d026e2";
			
			HttpClient client = new DefaultHttpClient();
		    HttpPost post = new HttpPost("https://api.justyo.co/yo/");
		    try {
		      ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(1);
		      	params.add(new BasicNameValuePair("api_token", token));
				params.add(new BasicNameValuePair("username", yoField.getText()));
				params.add(new BasicNameValuePair("link", urlBar.getText()));
		      post.setEntity(new UrlEncodedFormEntity(params));
		 
		      HttpResponse response = client.execute(post);
		      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		      String line = "";
		      while ((line = rd.readLine()) != null) 
		      {
		        System.out.println(line);
		      }

		    } 
		    catch (IOException e) 
		    {
		      e.printStackTrace();
		    }
		  }
			
	}
		
	
	/******************************************************************************
	* The following class is used as an action listener for when the user clicks the
	* back button. It only has one method that just handles the click of the back button.
	 ******************************************************************************/
	public static class backAction implements ActionListener
	{
		/**
		 * This method moves the page back one element in the linear history. It also
		 * properly adjusts the location in the branched history.
		 */
		public void actionPerformed (ActionEvent e)
		{
			if(placeInLinear <= 0)
			{
				System.out.println("Uhh, double protection is nice then");
			}
			else
			{
				placeInLinear--;
				String urlString = linearHistory.get(placeInLinear).getUrl();
				if(linearHistory.get(placeInLinear).wasEntered() == false)
				{
					currentNode = currentNode.getParent();
				}
				else
				{
					currentNode = currentNode.getRoot();
				}
					loadURL(urlString);
					urlBar.setText(urlString);
				

				if(placeInLinear == 0)
				{
					backButton.setEnabled(false);
				}
				forwardButton.setEnabled(true);

			}


		}
	}
	
	/********************************************************************************************************
	* The following class is used is an action listener for when the user clicks the "History" button.
	* The method has only one, large method that instantiates the new view.
	* *****************************************************************************************************/
	public static class historyAction implements ActionListener
	{
		/**
		 * This method shows the history view. It positions the frame correctly,
		 * then goes through each child of the root and adds them to the screen.
		 */
		public void actionPerformed (ActionEvent e)
		{
			historyFrame = new JFrame();
			historyFrame.setSize(750, 750);
			historyFrame.setResizable(false);
			historyFrame.setLocationRelativeTo(null);
			historyFrame.setVisible(true);
			JPanel panel = new JPanel();

			
			GridLayout layout = new GridLayout(currentNode.getRoot().getNumOfChildren(), 2, 5,10);
			panel.setLayout(layout);
			
//			scrollView.add(panel);
			historyFrame.add(panel);
			for (int i = currentNode.getRoot().getNumOfChildren() - 1; i >= 0; i--)
			{
			
				
				JButton arrowButton;
				System.out.println("Conents = " + currentNode.getRoot().getChildAtIndex(i).getContents());
				System.out.println("Number of children = " + currentNode.getRoot().getChildAtIndex(i).getNumOfChildren());
				if(currentNode.getRoot().getChildAtIndex(i).getNumOfChildren() == 0)
				{
					ImageIcon arrowImage = new ImageIcon("TriangleDownSmall.png");
					arrowButton = new JButton (arrowImage);
				}
				else
				{
					ImageIcon arrowImage = new ImageIcon("triangleRightSmall.png");
					arrowButton = new JButton (arrowImage);
					arrowButton.addActionListener(new displayChildrenHistory(currentNode.getRoot().getChildAtIndex(i)));
				}
				JPanel sizePanelArrow = new JPanel();
			    sizePanelArrow.add(arrowButton);
				
				String url = currentNode.getRoot().getChildAtIndex(i).getContents();
				JButton urlButton = new JButton (url);
				urlButton.addActionListener(new goFromHistory(url));
				JPanel sizePanelURL = new JPanel();
				sizePanelURL.add(urlButton);
				
				panel.add(sizePanelArrow);
				panel.add(sizePanelURL);
			}
			
		}
	}
	
	/******************************************************************************
	* The following class is used as an action listener such that if the user clicks
	* on a button in the history view it will move them to the current page.
	 ******************************************************************************/
	
	public static class goFromHistory implements ActionListener
	{
		private String url;
		/**
		 * This method serves as the class constructor. It simply assigns
		 * the parameter url to the variable stored in the class
		 * @param url	The ul of the button clicked
		 */
		private goFromHistory(String url)
		{
			this.url = url;
		}
		
		/**
		 * This method makes the current page the url of the button clicked and disables the history frame.
		 * It also adjusts the linear history to add the url as entered.
		 */
		public void actionPerformed (ActionEvent e)
		{
			currentNode = currentNode.getRoot();
			historyFrame.setVisible(false);
			linearHistory.add(new historyObject(true, url));
			placeInLinear = linearHistory.size() - 1;
			backButton.setEnabled(true);
			loadURL(url);
		}
	}
	
	/******************************************************************************
	* The following class is used is an action listener for when the user clicks the arrow
	* button that displays the history of the child of the selected node.
	 ******************************************************************************/
	public static class displayChildrenHistory implements ActionListener
	{
		treeNode node;
		
		/**
		 * This method serves as a constructor for this method by
		 * setting the parameter t to the treeNode of the parent
		 * @param t	The parent of the node the user wants displayed
		 */
		private displayChildrenHistory(treeNode t)
		{
			node = t;
		}
		
		/**
		 * This method creates a new JFrame under history frame to clear the old one
		 * then displays the children of treeNode node with the contents of node displayed at the top.
		 * It also handles the special case of if the parent is the root in which case it should not be displayed
		 * as the root always has the contents of null.
		 */
		public void actionPerformed (ActionEvent e)
		{
			historyFrame.setVisible(false);
			historyFrame = new JFrame();
			historyFrame.setSize(750, 750);
			historyFrame.setResizable(false);
			historyFrame.setLocationRelativeTo(null);
			historyFrame.setVisible(true);
			
			JPanel panel = new JPanel();
			GridLayout layout = new GridLayout(node.getNumOfChildren() + 1, 2, 5,10);
			panel.setLayout(layout);
			historyFrame.add(panel);
			JButton arrowButton;
{
			if(node.getContents() != null) 
			{
				
				ImageIcon arrowImageTop = new ImageIcon("TriangleDownSmall.png");
				arrowButton = new JButton (arrowImageTop);
				JPanel sizePanelArrowTop = new JPanel();
				if(node.getParent() == null)
				{
					arrowButton.addActionListener(new historyAction());
				}
				else
				{
					arrowButton.addActionListener(new displayChildrenHistory(node.getParent()));
				}
				
				
				sizePanelArrowTop.add(arrowButton);
				String url = node.getContents();
				JButton urlButtonTop = new JButton (url);
				urlButtonTop.addActionListener(new goFromHistory(url));
				JPanel sizePanelURLTop = new JPanel();
				sizePanelURLTop.add(urlButtonTop);
				panel.add(sizePanelArrowTop);
				panel.add(sizePanelURLTop);
			}
			
				
				//Cycle through children adding each to screen
				for (int i = node.getNumOfChildren()- 1; i >= 0; i--)
				{
					if(node.getChildAtIndex(i).getNumOfChildren() == 0)
					{
						ImageIcon arrowImage = new ImageIcon("TriangleDownSmall.png");
						arrowButton = new JButton (arrowImage);
					}
					else
					{
						ImageIcon arrowImage = new ImageIcon("triangleRightSmall.png");
						arrowButton = new JButton (arrowImage);
						arrowButton.addActionListener(new displayChildrenHistory(node.getChildAtIndex(i)));
					}
					JButton urlButton = new JButton(node.getChildAtIndex(i).getContents());
					urlButton.addActionListener(new goFromHistory(node.getChildAtIndex(i).getContents()));
					JPanel sizePanelUrl = new JPanel();
					JPanel sizePanelArrow = new JPanel();
					sizePanelArrow.add(arrowButton);
					sizePanelUrl.add(urlButton);
					panel.add(sizePanelArrow);
					panel.add(sizePanelUrl);
					
					
				}
			}
			
		}
		
	}
	
	/**
	 * This method initializes the JavaFX web browser and engine
	 */
    private void createScene() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                WebView view = new WebView();
                engine = view.getEngine();

                engine.titleProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                browserWindow.this.setTitle(newValue);
                            }
                        });
                    }
                });

                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    @Override
                    public void handle(final WebEvent<String> event) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                            	String data = event.getData();
                            	
                            	if(data != null && data.contains("http") == true && data.charAt(data.length() - 1) != '#')
                            	{
                            		lastMousedOver = data;
                            	}
                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            /**
                             * The following handles hyperlinks. Becuase JFX has no hyperlink listener a variable
                             * is stored of the last moused over and if this is called (which means its loading)
                             * and that is the last moused over it treats it as a hyperlink.
                             */
                            public void run() {
                            	if(lastMousedOver!= null && lastMousedOver.equals(newValue))
                            	{
                            		linearHistory.add(new historyObject (false, lastMousedOver));
                                	placeInLinear = linearHistory.size() - 1;
                                	treeNode temp = currentNode.getParent().nodeWith(urlBar.getText());
                                	if(temp == null)
                                	{
                                		currentNode.addChild(lastMousedOver);
                                		currentNode = currentNode.getLastChild();
                                	}
                                	else
                                	{
                                		temp.addChild(newValue);
                                		currentNode = temp.getLastChild();
                                		System.out.println(currentNode);
                                	}
                                	
                                	backButton.setEnabled(true);
                                	forwardButton.setEnabled(false);
                            	}
                                urlBar.setText(newValue);                          
                            }
                        });
                    }
                });

                engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                            	
                            }
                        });
                    }
                });

                engine.getLoadWorker()
                        .exceptionProperty()
                        .addListener(new ChangeListener<Throwable>() {

                            public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                                if (engine.getLoadWorker().getState() == FAILED) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            JOptionPane.showMessageDialog(
                                            navPanel,
                                            (value != null)
                                            ? engine.getLocation() + "\n" + value.getMessage()
                                            : engine.getLocation() + "\nUnexpected error.",
                                            "Loading error...",
                                            JOptionPane.ERROR_MESSAGE);
                                        }
                                    });
                                }
                            }
                        });

                jfxPanel.setScene(new Scene(view));
            }
        });
    }
    
    /**
     * This method sets the URL entered to the URL loaded by the web engine. As well,
     * if necessary it adds the http:// and checks to see if the user entered a twitter handle
     * in which case it just redirects to their twitter page.
     * @param url	The url entered by the user
     */
    private static void loadURL(String url) {
    	
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	String tmp;
            	if(url.charAt(0) == '@')
            	{
            		System.out.println("Aquí");
            		String twitterUsername = url.substring(1);
            		String twitterURL = "http://twitter.com/" + twitterUsername; 
            		tmp  = toURL(twitterURL);
            	}
            	else
            	{
            		tmp = toURL(url);
            		tmp = toURL(url);

                    if (tmp == null) 
                    {
                        tmp = toURL("http://" + url);
                    }
            	}
            	
                currentNode.getRoot().addChild(tmp);
                
                currentNode = currentNode.getRoot().getLastChild();
                engine.load(tmp);
            }
        });
        

    }

    /**
     * This method determines if the URL is real or not. It attempts
     * to convert it to a URL then if an exception is thrown it knows
     * that this isn't a real URL.
     * @param str
     * @return
     */
    private static String toURL(String str) 
    {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
            return null;
        }
    }
    
    /**
     * The constructor for the browser window. It simply
     * adds all of the necessary window elements.
     */
    public browserWindow()
    {
    	super();
    	addWindowElements();
    }
    
    

   
    /**
     * This method initializes the web view by calling createScene then adds the UI elements of the browser
     */
    private void addWindowElements()
    {
    	createScene();
    	
		backButton.setEnabled(false);
		forwardButton.setEnabled(false);
		backButton.addActionListener(new backAction());
		forwardButton.addActionListener(new forwardAction());
		urlBar.addActionListener(new NavAction());
		navPanel.setLayout(new FlowLayout());
		navPanel.addKeyListener(this);
		navPanel.add(backButton);
		navPanel.add(forwardButton);
		navPanel.add(urlBar);
		navPanel.add(yoButton);
		navPanel.add(historyButton);
		historyButton.addActionListener(new historyAction());
		yoButton.addActionListener(new initYoView());
		this.add(jfxPanel);
		jfxPanel.addKeyListener(this);
		this.add(navPanel, BorderLayout.NORTH);
		setPreferredSize(new Dimension(1024, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
		
    }
    
    
	
	
	
}
