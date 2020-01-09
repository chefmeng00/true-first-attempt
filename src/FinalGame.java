//AP Comp Sci
//Original Author: Meng Chau
//"Space Fighter"
//some sounds from zapsplat.com, AlawarTM games and it's partners, most art by Xuan Chau, explosion animation by sinestesiaTM

import java.awt.event.MouseAdapter; //for mouse input reception
import java.awt.event.MouseEvent; //for mouse inputs
import java.awt.event.MouseWheelEvent; //for mouse wheel inputs
import java.util.TimerTask; //for "threaded" tasks in a timer
import java.util.ArrayList; //for array lists 
import java.util.Random; //for random generation
import java.util.Timer; //for timer threads
import java.awt.*; //for GUI, interfaces, graphics, etc
import java.io.*; //for file reading
import javax.sound.sampled.*; //for sound/audio
import javax.swing.*; //for graphics components, panels, frames, etc.

public class FinalGame {

  //whole application frame
  static JFrame thisFrame = null;

  static Sound menuMusic;
  static Sound gameMusic;
  static boolean soundOn = true;

  //loading screen components
  static int loadProgression = 0;
  static Timer loadTimer;
  static String loadState = null;

  //Game display data
  static Timer statement;
  static double introTimePassed = 0;
  static int introSizeChange = 0;
  static JLabel shipImage;
  static JLayeredPane GameScreen; //need layered pane because components like bullets, ships, etc will overlap
  static JLabel background;    
  static Ship ship;   
  static boolean dead = false; //used to "toggle" ship damage on or off
  static boolean gameActive = false; //indicates as to whether or not the game functions can start 

  static boolean isPaused = false; //halt game functions until reset to false
  static boolean pausing = false; //when the indicator is physically moving 
  static int shift = 0;
  static Timer pauseFlow;

  static graphicsComponent healthBar;
  static graphicsComponent heatBar;
  static graphicsComponent fuelBar;
  static Color fuelBarColor = new Color(0, 255, 0); //starting out green, transitions to red
  static Color healthBarColor = new Color(150, 0, 0); //starting out dark red health bar
  static Color heatBarColor = new Color(255, 255, 0); //starting out relatively light blue
  static JLabel currentEffect; //manages the effect indicator
  static double durationBarLengthRatio; //determines the length of the bar relative to the duration of each drop
  static graphicsComponent durationBar;

  //"tick" timers are timers that don't guarantee an effect, but rather will check for a condition to complete a run task
  static Timer enemySpawnTickTimer; 
  static Timer spawnDropTickTimer;
  static Timer spawnWeaponTickTimer;
  static Timer cooldownTimer;
  static Timer depleteEnergyTimer;
  static Timer scoreIncreaseTimer;
  static Timer shootingTimer;
  
  static int currentScore = 0; //current data needed to update ship statistic HUD text 
  static int currentScoreDisplayed = 0; //this number is the number that is shown on screen at any moment
  static int currentHeatState = 0; //cap of 100 for simplicity
  static int currentEnergyState = 100; //cap of 100 for simplicity. decrements by 20
  static int energyColorDeterminant = (int)((255.0 / 100.0) * (double)currentEnergyState);
  static boolean isOverheated = false;
  static int timeLeft = 0; //time REMAINING of the current effect

  //special weapon inventory must be static to be modified
  static JLabel[] inventories; //to keep track of the selected weapon to highlight the labels at the top
  static String[] weaponImagePaths; //keep track of special weapon image file names
  static int weaponSelection = 0;
  static JLabel rockets;
  static JLabel smartRockets;
  static JLabel lasers;
  static JLabel machineBlaster;
  static JLabel spreadBlast;
  static int rocketCount = 0;
  static int smartRocketCount = 0;
  static int laserCount = 0;
  static int machineBlasterCount = 0;
  static int spreadBlastCount = 0;

  static boolean rapid = false; //static drop detection booleans to switch lingering power ups on and off
  static boolean invincible = false;
  static boolean enemyFireDisabled = false;

  //death screen components
  static int currentHighscore = 0;
  static double shotsFired = 0;
  static double shotsImpacted = 0;

  static ArrayList<Enemy> enemies = new ArrayList<Enemy>(); //enemies on screen at the moment
  static ArrayList<Drop> powerups = new ArrayList<Drop>(); //to monitor all active or caught drops to ensure that the correct effect is in use and that the next game is unaffected by it

  public static void main(String[] args) {
    setupMenu(true, true);
  }


  public static void setupMenu(boolean newFrame, boolean playMusic) { //used to set up the menu

    if(newFrame) { //only if opening the application for the first time should the program create a new frame
      thisFrame = new JFrame("Meng Chau Space Shooter"); 
      thisFrame.setUndecorated(true);
      thisFrame.setExtendedState(thisFrame.getExtendedState() | Frame.MAXIMIZED_BOTH); //will set the frame size to the maximum width and height of all the components
      thisFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //used to close frame upon calling an system exit function
    } 
    if(playMusic) //whether or not to play music
      menuMusic = new Sound("Sounds/menuMusic.wav");

    JPanel mainPanel = new JPanel(new GridLayout(11, 1)); //1 column cell indicates that the mainPanel screen should have a single centered column of components. similar to BoxLayout
    mainPanel.setBackground(Color.BLACK);
    mainPanel.setPreferredSize(new Dimension(thisFrame.getWidth(), thisFrame.getHeight())); //with MAXIMIZED_BOTH, size this panel to the size of the frame so it stays full screen    

    JLabel titleText = new JLabel(new ImageIcon("Images/titleImage.png"));
    mainPanel.add(titleText);

    JLabel fineTitle = new JLabel("~Created by Meng Chau~", SwingConstants.CENTER);
    mainPanel.add(fineTitle);
    fineTitle.setFont(new Font("timesRoman", Font.ITALIC, 15));
    fineTitle.setForeground(Color.WHITE);

    //blank Labels used to allow for y position customization within a layout manager. As more are added, the components will move to accommodate them
    mainPanel.add(new Label());    

    JPanel playPanel = new JPanel(); //my buttons/indicators will be present on new separate individual panels in order to allow overlapping along with proportional orientation
    playPanel.setBackground(Color.BLACK);
    JLabel playIndicatorLeft = new JLabel(">>  ");
    JLabel playIndicatorRight = new JLabel("  <<");
    playIndicatorLeft.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    playIndicatorRight.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    playIndicatorLeft.setForeground(new Color(255, 150, 0));
    playIndicatorRight.setForeground(new Color(255, 150, 0));
    playIndicatorLeft.setVisible(false);
    playIndicatorRight.setVisible(false);
    JLabel playLabel = new JLabel("[START GAME]");
    playLabel.setForeground(Color.WHITE);
    playLabel.setFont(new Font("TimesRoman", Font.PLAIN, 30));

    playPanel.add(playIndicatorLeft); //Due to the nature of layout managers which tend to flow horizontally per component, each button panel must added in as "> [label] <"       
    playPanel.add(playLabel);
    playPanel.add(playIndicatorRight);
    mainPanel.add(playPanel);

    mainPanel.add(new Label());  

    JPanel howToPanel = new JPanel();
    howToPanel.setBackground(Color.BLACK);
    JLabel howToIndicatorLeft = new JLabel(">>  ");
    JLabel howToIndicatorRight = new JLabel("  <<");
    howToIndicatorLeft.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    howToIndicatorRight.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    howToIndicatorLeft.setForeground(new Color(255, 150, 0));
    howToIndicatorRight.setForeground(new Color(255, 150, 0));
    howToIndicatorLeft.setVisible(false);
    howToIndicatorRight.setVisible(false);
    JLabel howToLabel = new JLabel("[INSTRUCTIONS]");
    howToLabel.setForeground(Color.WHITE);
    howToLabel.setFont(new Font("TimesRoman", Font.PLAIN, 30));

    howToPanel.add(howToIndicatorLeft);
    howToPanel.add(howToLabel);
    howToPanel.add(howToIndicatorRight);
    mainPanel.add(howToPanel);

    mainPanel.add(new Label());

    JPanel quitPanel = new JPanel();
    quitPanel.setBackground(Color.BLACK);
    JLabel quitIndicatorLeft = new JLabel(">>  ");
    JLabel quitIndicatorRight = new JLabel("  <<");
    quitIndicatorLeft.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    quitIndicatorRight.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    quitIndicatorLeft.setForeground(new Color(255, 150, 0));
    quitIndicatorRight.setForeground(new Color(255, 150, 0));
    quitIndicatorLeft.setVisible(false);
    quitIndicatorRight.setVisible(false);
    JLabel quitLabel = new JLabel("[QUIT]", SwingConstants.CENTER);
    quitLabel.setForeground(Color.WHITE);
    quitLabel.setFont(new Font("TimesRoman", Font.PLAIN, 30));

    quitPanel.add(quitIndicatorLeft);
    quitPanel.add(quitLabel);
    quitPanel.add(quitIndicatorRight);
    mainPanel.add(quitPanel);

    mainPanel.add(new Label());
    mainPanel.add(new Label());
    JPanel soundTogglePanel = new JPanel();
    soundTogglePanel.setBackground(Color.BLACK);

    JLabel soundLabel = new JLabel("Sound [ON/OFF]");
    soundLabel.setForeground(new Color(160, 0, 0));
    soundLabel.setFont(new Font("TimesRoman", Font.PLAIN, 20));   

    JLabel soundToggleImage;
    if (soundOn) {
      soundToggleImage = new JLabel(new ImageIcon("Images/soundEnabled.png"));
    } else {
      soundToggleImage = new JLabel(new ImageIcon("Images/blankDrop.png"));
    }
    soundTogglePanel.add(soundLabel);
    soundTogglePanel.add(soundToggleImage);

    mainPanel.add(soundTogglePanel);


    thisFrame.add(mainPanel);
    thisFrame.setVisible(true);

    playLabel.addMouseListener(new MouseAdapter() { //implements mouse functions of the mouseListener for certain components of your choice to listen for mouse activity      
      public void mouseReleased(MouseEvent e) {
        thisFrame.remove(mainPanel);
        new Sound("Sounds/clickSound.wav");
        loading();
      }
      public void mouseEntered(MouseEvent e) {
        playIndicatorLeft.setVisible(true);
        playIndicatorRight.setVisible(true);
        playLabel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        playLabel.setForeground(new Color(130, 130, 130));
        new Sound("Sounds/mouseOverSound.wav");
      }
      public void mouseExited(MouseEvent e) {
        playIndicatorLeft.setVisible(false);
        playIndicatorRight.setVisible(false);
        playLabel.setForeground(Color.WHITE);
      }
    }
        );

    howToLabel.addMouseListener(new MouseAdapter() {            
      public void mouseReleased(MouseEvent e) {
        thisFrame.remove(mainPanel);
        new Sound("Sounds/clickSound.wav");
        instruct();
      }           
      public void mouseEntered(MouseEvent e) {
        howToIndicatorLeft.setVisible(true);
        howToIndicatorRight.setVisible(true);
        howToLabel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        howToLabel.setForeground(new Color(130, 130, 130));
        new Sound("Sounds/mouseOverSound.wav");
      }
      public void mouseExited(MouseEvent e) {
        howToIndicatorLeft.setVisible(false);
        howToIndicatorRight.setVisible(false);
        howToLabel.setForeground(Color.WHITE);
      }
    }
        );

    quitLabel.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        new Sound("Sounds/clickSound.wav");
        System.exit(1);
      }           
      public void mouseEntered(MouseEvent e) {
        quitIndicatorLeft.setVisible(true);
        quitIndicatorRight.setVisible(true);
        quitLabel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        quitLabel.setForeground(new Color(130, 130, 130));
        new Sound("Sounds/mouseOverSound.wav");
      }
      public void mouseExited(MouseEvent e) {
        quitIndicatorLeft.setVisible(false);
        quitIndicatorRight.setVisible(false);
        quitLabel.setForeground(Color.WHITE);
      }
    }
        );

    soundToggleImage.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        new Sound("Sounds/clickSound.wav");
        if(soundOn) { //sound turning off
          soundToggleImage.setIcon(new ImageIcon("Images/blankDrop.png"));
          soundOn = false;
          menuMusic.getClip().stop(); //stop menu music instantly rather than muting it. Prevent sound from being played 
        }
        else { //sound turning on
          soundToggleImage.setIcon(new ImageIcon("Images/soundEnabled.png"));
          soundOn = true;
          menuMusic = new Sound("Sounds/menuMusic.wav"); //restart menu music and allow sound to be played
        }
      }           
      public void mouseEntered(MouseEvent e) {
        soundToggleImage.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        soundLabel.setForeground(Color.RED);
        new Sound("Sounds/mouseOverSound.wav");
      }
      public void mouseExited(MouseEvent e) {
        soundLabel.setForeground(new Color(170, 0, 0));
      }
    }
        );

  }



  public static void instruct() {

    JPanel instructionPanel1 = new JPanel(); //set up instructions panel screen  
    instructionPanel1.setLayout(new BoxLayout(instructionPanel1, BoxLayout.Y_AXIS)); //order components from top to bottom
    instructionPanel1.setBackground(Color.BLACK);
    instructionPanel1.setPreferredSize(new Dimension(thisFrame.getWidth(), thisFrame.getHeight()));

    //add some space before instructions image for appearance
    instructionPanel1.add(new Label());

    JLabel instructions = new JLabel(new ImageIcon("Images/instructionsImage.png"));
    instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
    instructionPanel1.add(instructions);

    instructionPanel1.add(new Label());

    JLabel next = new JLabel("[>>>]");  
    next.setForeground(Color.WHITE);
    next.setAlignmentX(Component.CENTER_ALIGNMENT);
    next.setFont(new Font("TimesRoman", Font.PLAIN, 30));

    JPanel back = new JPanel(); //sets up button label and indicator panel to be added into the current page panel
    back.setBackground(Color.BLACK);
    JLabel backIndicatorLeft = new JLabel(">>  ");
    JLabel backIndicatorRight = new JLabel("  <<");
    backIndicatorLeft.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    backIndicatorRight.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    backIndicatorLeft.setForeground(new Color(255, 150, 0));
    backIndicatorRight.setForeground(new Color(255, 150, 0));
    backIndicatorLeft.setVisible(false);
    backIndicatorRight.setVisible(false);
    JLabel leaveLabel = new JLabel("[BACK]"); //indicator and label are placed before so that the image does not layer on top of my components
    leaveLabel.setForeground(Color.WHITE);
    leaveLabel.setFont(new Font("TimesRoman", Font.PLAIN, 30));

    back.add(backIndicatorLeft);
    back.add(leaveLabel);
    back.add(backIndicatorRight);

    instructionPanel1.add(next);
    instructionPanel1.add(back);
    instructionPanel1.add(new Label());
    instructionPanel1.add(new Label());

    thisFrame.add(instructionPanel1);    
    thisFrame.setVisible(true);

    next.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {               
        new Sound("Sounds/clickSound.wav");
        thisFrame.remove(instructionPanel1);
        instructPage2();
      }           
      public void mouseEntered(MouseEvent e) {
        next.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        next.setForeground(new Color(130, 130, 130));
        new Sound("Sounds/mouseOverSound.wav");
      }
      public void mouseExited(MouseEvent e) {
        next.setForeground(Color.WHITE);
      }
    });

    leaveLabel.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        thisFrame.remove(instructionPanel1); //same idea as when removing the menu panel, remove instructions panel.
        setupMenu(false, false); //call false so that a new frame is not created and all components, labels, and panels stay in the same interface
        new Sound("Sounds/clickSound.wav");
      }           
      public void mouseEntered(MouseEvent e) {
        backIndicatorLeft.setVisible(true);
        backIndicatorRight.setVisible(true);
        leaveLabel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        leaveLabel.setForeground(new Color(130, 130, 130));
        new Sound("Sounds/mouseOverSound.wav");
      }
      public void mouseExited(MouseEvent e) {
        backIndicatorLeft.setVisible(false);
        backIndicatorRight.setVisible(false);
        leaveLabel.setForeground(Color.WHITE);
      }
    });

  }                                        



  public static void instructPage2() {
    JPanel instructionPanel2 = new JPanel();   
    instructionPanel2.setLayout(new BoxLayout(instructionPanel2, BoxLayout.Y_AXIS)); 
    instructionPanel2.setBackground(Color.BLACK);
    instructionPanel2.setPreferredSize(new Dimension(thisFrame.getWidth(), thisFrame.getHeight()));

    JLabel header = new JLabel(">> [POWER UPS AND DROPS] <<");
    header.setForeground(new Color(255, 150, 0));
    header.setAlignmentX(Component.CENTER_ALIGNMENT);
    header.setFont(new Font("TimesRoman", Font.BOLD, 30));      
    instructionPanel2.add(header);

    String dropPaths[] = new String[] {"Images/fuelDrop.png", "Images/cooldownDrop.png", "Images/rapidfireDrop.png", "Images/invincibilityDrop.png", "Images/healthDrop.png", "Images/disableDrop.png", "Images/extraScoreDrop.png"};
    String dropDescriptions[] = new String[] {"   -[INSTANT EFFECT]- Regenerates your fuel meter by one unit", "   -[INSTANT EFFECT]- Completely cools down your ships core heat to zero",
        "   -[LASTING EFFECT]- Double the firing rate of your main weapon for the same heat output for 15 seconds", "   -[LASTING EFFECT]- Gives your ship immunity from any damage for 10 seconds",
        "   -[INSTANT EFFECT]- Repairs your ship by 200 health units", "   -[LASTING EFFECT]- A shockwave that temporarily disables the enemies ability to fire weapons for 15 seconds",
    "   Collect gold stars from some destroyed ships to boost your score!"};        

    //description and images of each of the different possible non weapon drops
    for(int i = 0; i < dropPaths.length && dropPaths.length == dropDescriptions.length; i++) {
      JPanel dropInfo = new JPanel(); 
      dropInfo.setLayout(new BoxLayout(dropInfo, BoxLayout.X_AXIS));
      dropInfo.setBackground(Color.BLACK);
      dropInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
      dropInfo.add(new JLabel(new ImageIcon(dropPaths[i])));

      if(dropPaths[i].equals("Images/extraScoreDrop.png"))
        instructionPanel2.add(new Label());

      JLabel dropDescription = new JLabel(dropDescriptions[i]);         
      dropDescription.setFont(new Font("TimesRoman", Font.ITALIC + Font.BOLD, 20));
      dropDescription.setForeground(Color.WHITE);
      dropInfo.add(dropDescription);

      instructionPanel2.add(dropInfo);
    }               

    JPanel navigate = new JPanel();
    navigate.setBackground(Color.BLACK);
    navigate.setLayout(new BoxLayout(navigate, BoxLayout.X_AXIS));

    JLabel next = new JLabel("[>>>]");  
    next.setForeground(Color.WHITE);
    next.setFont(new Font("TimesRoman", Font.PLAIN, 30));

    JLabel previous = new JLabel("[<<<]");
    previous.setForeground(Color.WHITE);
    previous.setFont(new Font("TimesRoman", Font.PLAIN, 30));

    instructionPanel2.add(new Label());
    navigate.add(previous);
    navigate.add(next);
    instructionPanel2.add(navigate);
    instructionPanel2.add(new Label());
    instructionPanel2.add(new Label());

    thisFrame.add(instructionPanel2);    
    thisFrame.setVisible(true);

    previous.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {               
        new Sound("Sounds/clickSound.wav");
        thisFrame.remove(instructionPanel2);
        instruct();
      }           
      public void mouseEntered(MouseEvent e) {
        previous.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        previous.setForeground(new Color(130, 130, 130));
        new Sound("Sounds/mouseOverSound.wav");
      }
      public void mouseExited(MouseEvent e) {
        previous.setForeground(Color.WHITE);
      }
    });

    next.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {               
        new Sound("Sounds/clickSound.wav");
      }           
      public void mouseEntered(MouseEvent e) {
        next.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        next.setForeground(new Color(130, 130, 130));
        new Sound("Sounds/mouseOverSound.wav");
      }
      public void mouseExited(MouseEvent e) {
        next.setForeground(Color.WHITE);
      }
    });

  }



  public static void loading() {
    JPanel loadingPanel = new JPanel(); //whole loading screen frame
    loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
    loadingPanel.setBackground(Color.BLACK);
    loadingPanel.setPreferredSize(new Dimension(thisFrame.getWidth(), thisFrame.getHeight()));

    JLabel splashImage = new JLabel(new ImageIcon("Images/loadingImage1.png"));
    loadState = "flameOff";
    splashImage.setAlignmentX(Component.CENTER_ALIGNMENT);  

    new Timer().scheduleAtFixedRate(new TimerTask() {
      public void run() {
        if(loadState.equals("flameOff")) {
          splashImage.setIcon(new ImageIcon("Images/loadingImage2.png"));
          loadState = "flameOn";
        }
        else if(loadState.equals("flameOn")) {
          splashImage.setIcon(new ImageIcon("Images/loadingImage1.png"));
          loadState = "flameOff";
        }
      }
    }, 0, 15);

    graphicsComponent loadingBar = new graphicsComponent("loadingBar", (thisFrame.getWidth() / 2) - 125,
        (thisFrame.getHeight() / 2) + 50, 250, 15);

    loadingPanel.add(splashImage);
    loadingPanel.add(loadingBar);

    TimerTask loadGame = new TimerTask() {
      public void run() {
        loadingBar.load(); //load the bar
        if(loadProgression == 250) {
          thisFrame.remove(loadingPanel);
          menuMusic.getClip().stop();                   
          play(); //start game
        }
      }
    };
    loadTimer = new Timer("loadTimer");
    loadTimer.scheduleAtFixedRate(loadGame, 0, 15);

    thisFrame.add(loadingPanel);      
    thisFrame.setVisible(true);
  }



  public static void play() {

    int musicTrack = (int)(Math.random() * 3) + 1;
    gameMusic = new Sound("Sounds/gameMusic" + musicTrack + ".wav"); 
    while(gameActive) { //while playing the game, check if the current game play music is playing or has ended
      if(!gameMusic.getClip().isActive()) { //if the music track has ended
        musicTrack++;
        if(musicTrack > 4) { //cycle through all 4 game play music tracks
          musicTrack = 1;
        }
        new Sound("Sounds/gameMusic" + musicTrack + ".wav");  //play the next track
      }
    }
    GameScreen = new JLayeredPane();
    GameScreen.setPreferredSize(new Dimension(thisFrame.getWidth(), thisFrame.getHeight()));
    thisFrame.add(GameScreen); 

    background = new JLabel(new ImageIcon("Images/gameBackground.png"));
    background.setPreferredSize(new Dimension(thisFrame.getWidth(), thisFrame.getHeight()));
    background.setBounds(new Rectangle(new Point(0,0), background.getPreferredSize()));

    //when creating an instance of the ship initially, it should be at the users cursor and above the background
    int shipX = (int)MouseInfo.getPointerInfo().getLocation().getX();
    int shipY = (int)MouseInfo.getPointerInfo().getLocation().getY();
    ship = new Ship(shipX, shipY, 1000, "Regular Bullets"); //the health can be changed by modifying this ship

    GameScreen.add(background);             

    JLabel prepStatement = new JLabel("[ GET READY ]"); //pops out at the beginning of each game 
    prepStatement.setFont(new Font("Times Roman", Font.ITALIC + Font.BOLD, introSizeChange)); //start out invisible
    prepStatement.setBounds(new Rectangle(new Point((GameScreen.getWidth() / 2) - (int)(prepStatement.getPreferredSize().getWidth() / 2),
        (GameScreen.getHeight() / 2) - (int)(prepStatement.getPreferredSize().getHeight() / 2)), prepStatement.getPreferredSize()));
    prepStatement.setForeground(Color.YELLOW);
    GameScreen.add(prepStatement);
    GameScreen.moveToFront(prepStatement);

    TimerTask introduce = new TimerTask() {
      public void run() {
        if(introTimePassed < 30) {
          introSizeChange += 1;
          prepStatement.setFont(new Font("TimesRoman", Font.PLAIN, introSizeChange)); //start out invisible
          prepStatement.setBounds(new Rectangle(new Point((GameScreen.getWidth() / 2) - (int)(prepStatement.getPreferredSize().getWidth() / 2),
              (GameScreen.getHeight() / 2) - (int)(prepStatement.getPreferredSize().getHeight() / 2)), prepStatement.getPreferredSize()));
        }
        if(introTimePassed > 1500) { //remove label and start game
          prepStatement.setVisible(false);
          GameScreen.remove(prepStatement);
          gameActive = true;
          statement.cancel();
          introTimePassed = 0;
        }
        introTimePassed += 1;
      }
    };
    statement = new Timer("statement");
    statement.scheduleAtFixedRate(introduce, 0, 2);

    

    //all on screen components like weapon inventories, bars, drop indicator, etc, are sized/placed relative to screen size
    fuelBar = new graphicsComponent("fuelBar", thisFrame.getWidth() - (thisFrame.getWidth() / 5)
        - (thisFrame.getWidth() / 30), 20, thisFrame.getWidth() / 5, 20); //graphical components for info meters
    healthBar = new graphicsComponent("healthBar", thisFrame.getWidth() - (2 * (thisFrame.getWidth() / 5))
        - (2 * (thisFrame.getWidth() / 30)), 20, thisFrame.getWidth() / 5, 20);
    heatBar = new graphicsComponent("heatBar", thisFrame.getWidth() - (3 * (thisFrame.getWidth() / 5))
        - (3 * (thisFrame.getWidth() / 30)), 20, thisFrame.getWidth() / 5, 20);

    //canvases cover whole screen. respective info meters are then bounded/sized accordingly
    JLayeredPane fuelMeterCanvas = new JLayeredPane(); 
    fuelMeterCanvas.setPreferredSize(new Dimension(thisFrame.getWidth(), thisFrame.getHeight()));
    fuelMeterCanvas.setLayout(new BoxLayout(fuelMeterCanvas, BoxLayout.X_AXIS));
    fuelMeterCanvas.setBounds(new Rectangle(new Point(0, 0), fuelMeterCanvas.getPreferredSize()));
    fuelMeterCanvas.setOpaque(false); //makes it a transparent panel

    JLayeredPane heatMeterCanvas = new JLayeredPane(); 
    heatMeterCanvas.setPreferredSize(new Dimension(thisFrame.getWidth(), thisFrame.getHeight()));
    heatMeterCanvas.setLayout(new BoxLayout(heatMeterCanvas, BoxLayout.X_AXIS));
    heatMeterCanvas.setBounds(new Rectangle(new Point(0, 0), heatMeterCanvas.getPreferredSize()));
    heatMeterCanvas.setOpaque(false); 

    JLayeredPane healthMeterCanvas = new JLayeredPane(); 
    healthMeterCanvas.setPreferredSize(new Dimension(thisFrame.getWidth(), thisFrame.getHeight()));
    healthMeterCanvas.setLayout(new BoxLayout(healthMeterCanvas, BoxLayout.X_AXIS));
    healthMeterCanvas.setBounds(new Rectangle(new Point(0, 0), healthMeterCanvas.getPreferredSize()));
    healthMeterCanvas.setOpaque(false); 

    JLabel fuelText = new JLabel("[SHIP FUEL]");
    JLabel heatText = new JLabel("[THERMAL STATE]");
    JLabel healthText = new JLabel("[SHIP HEALTH]"); 
    fuelText.setFont(new Font("Times Roman", Font.PLAIN, //fonts are relative to the display are
        13 + (thisFrame.getWidth() * thisFrame.getHeight()) / (int)Math.pow(10, 6)));
    heatText.setFont(new Font("Times Roman", Font.PLAIN, 
        13 + (thisFrame.getWidth() * thisFrame.getHeight()) / (int)Math.pow(10, 6)));
    healthText.setFont(new Font("Times Roman", Font.PLAIN, 
        13 + (thisFrame.getWidth() * thisFrame.getHeight()) / (int)Math.pow(10, 6)));
    fuelText.setForeground(new Color(150, 255, 0));
    heatText.setForeground(new Color(255, 200, 0));
    healthText.setForeground(Color.RED);
    fuelText.setBounds(new Rectangle(new Point(fuelBar.xCorner 
        + (fuelBar.width / 2) - (fuelText.getPreferredSize().width / 2),
        50), fuelText.getPreferredSize())); 
    heatText.setBounds(new Rectangle(new Point(heatBar.xCorner
        + (healthBar.width / 2) - (heatText.getPreferredSize().width / 2),
        50), heatText.getPreferredSize()));
    healthText.setBounds(new Rectangle(new Point(healthBar.xCorner
        + (healthBar.width / 2) - (healthText.getPreferredSize().width / 2),
        50), healthText.getPreferredSize()));
    GameScreen.add(fuelText);
    GameScreen.moveToFront(fuelText);
    GameScreen.add(heatText);
    GameScreen.moveToFront(heatText);
    GameScreen.add(healthText);
    GameScreen.moveToFront(healthText);

    fuelMeterCanvas.add(fuelBar);
    heatMeterCanvas.add(heatBar);
    healthMeterCanvas.add(healthBar);
    fuelMeterCanvas.moveToFront(fuelBar);
    heatMeterCanvas.moveToFront(heatBar);
    healthMeterCanvas.moveToFront(healthBar);

    GameScreen.add(fuelMeterCanvas);
    GameScreen.moveToFront(fuelMeterCanvas);    
    GameScreen.add(heatMeterCanvas);
    GameScreen.moveToFront(heatMeterCanvas);
    GameScreen.add(healthMeterCanvas);
    GameScreen.moveToFront(healthMeterCanvas);
       
    
    currentEffect = new JLabel(); //starts with blank drop
    ImageIcon image = new ImageIcon("Images/blankDrop.png");
    Image scaledImage = image.getImage().getScaledInstance(image.getIconWidth() + thisFrame.getWidth() / 150,
        image.getIconHeight() + thisFrame.getWidth() / 150, java.awt.Image.SCALE_SMOOTH);
    currentEffect.setIcon(new ImageIcon(scaledImage)); //set the default blank icon to the finalized icon
    
    //bounds for effect icon set here so it is relative to the location of the duration bar, vice versa
    currentEffect.setBounds(new Rectangle(new Point((int)(currentEffect.getPreferredSize().getWidth() / 3), 
        thisFrame.getHeight() - (int)currentEffect.getPreferredSize().getHeight()
        - (int)(currentEffect.getPreferredSize().getWidth() / 3)), currentEffect.getPreferredSize()));
    
    durationBar = new graphicsComponent("durationBar", (int)(currentEffect.getLocation().getX()
        + (4 * currentEffect.getWidth()) / 3), (int)(currentEffect.getLocation().getY()
        - 10 + currentEffect.getHeight() / 2), 2 * currentEffect.getWidth(), 20);
    //yCorner -10 to center vertically with indicator since height of bar is 20
    
    JLayeredPane durationBarCanvas = new JLayeredPane(); 
    durationBarCanvas.setPreferredSize(new Dimension(thisFrame.getWidth(), thisFrame.getHeight()));
    durationBarCanvas.setLayout(new BoxLayout(durationBarCanvas, BoxLayout.X_AXIS));
    durationBarCanvas.setBounds(new Rectangle(new Point(0, 0), durationBarCanvas.getPreferredSize()));
    durationBarCanvas.setOpaque(false); 
    
    GameScreen.add(currentEffect);
    GameScreen.moveToFront(currentEffect);
    durationBarCanvas.add(durationBar);
    GameScreen.add(durationBarCanvas);
    GameScreen.moveToFront(durationBarCanvas);
    
    
    inventories = new JLabel[5]; //initialize label array of size n weapons

    //indicators for second hand weapons and inventory
    JPanel secondaryWeapons = new JPanel(); //pops out at the beginning of each game 
    secondaryWeapons.setPreferredSize(new Dimension(inventories.length * 50, 100)); 
    secondaryWeapons.setLayout(new BoxLayout(secondaryWeapons, BoxLayout.X_AXIS)); 
    secondaryWeapons.setBounds(new Rectangle(new Point(10,0), secondaryWeapons.getPreferredSize())); //small gap from screen
    secondaryWeapons.setOpaque(false); //makes it a clear panel
    GameScreen.add(secondaryWeapons);
    GameScreen.moveToFront(secondaryWeapons);

    rockets = new JLabel("[" + rocketCount + "]"); //inventory quantity labels
    rockets.setFont(new Font("Times Roman", Font.PLAIN, //fonts are also sized relative to the display size
        13 + (thisFrame.getWidth() * thisFrame.getHeight()) / (int)Math.pow(10, 6)));
    rockets.setAlignmentX(Component.CENTER_ALIGNMENT);
    rockets.setForeground(Color.RED);
    inventories[0] = rockets; //sets the quantity display to the number starting number of each weapon

    lasers = new JLabel("[" + laserCount + "]");
    lasers.setFont(new Font("Times Roman", Font.PLAIN, 
        13 + (thisFrame.getWidth() * thisFrame.getHeight()) / (int)Math.pow(10, 6)));
    lasers.setAlignmentX(Component.CENTER_ALIGNMENT);
    lasers.setForeground(Color.WHITE);
    inventories[1] = lasers;

    smartRockets = new JLabel("[" + smartRocketCount + "]");
    smartRockets.setFont(new Font("Times Roman", Font.PLAIN, 
        13 + (thisFrame.getWidth() * thisFrame.getHeight()) / (int)Math.pow(10, 6)));
    smartRockets.setAlignmentX(Component.CENTER_ALIGNMENT);
    smartRockets.setForeground(Color.WHITE);
    inventories[2] = smartRockets;

    machineBlaster = new JLabel("[" + machineBlasterCount + "]");
    machineBlaster.setFont(new Font("Times Roman", Font.PLAIN, 
        13 + (thisFrame.getWidth() * thisFrame.getHeight()) / (int)Math.pow(10, 6)));
    machineBlaster.setAlignmentX(Component.CENTER_ALIGNMENT);
    machineBlaster.setForeground(Color.WHITE);  
    inventories[3] = machineBlaster;

    spreadBlast = new JLabel("[" + spreadBlastCount + "]");
    spreadBlast.setFont(new Font("Times Roman", Font.PLAIN, 
        13 + (thisFrame.getWidth() * thisFrame.getHeight()) / (int)Math.pow(10, 6)));
    spreadBlast.setAlignmentX(Component.CENTER_ALIGNMENT);
    spreadBlast.setForeground(Color.WHITE);  
    inventories[4] = spreadBlast;

    weaponImagePaths = new String[] {"Images/rocketWeapon.png", "Images/laserWeapon.png",
        "Images/smartRocketWeapon.png", "Images/machineBlastWeapon.png", "Images/spreadBlastWeapon.png"};         

    for (int i = 0; i < weaponImagePaths.length && weaponImagePaths.length == inventories.length; i++) {
      JPanel weapon = new JPanel(); //four cells for each of the four weapons and their image + inventory
      weapon.setLayout(new BoxLayout(weapon, BoxLayout.Y_AXIS));
      weapon.setOpaque(false); //makes the weapon cell panels clear
      JLabel weaponImage = new JLabel(new ImageIcon(weaponImagePaths[i]));
      weaponImage.setAlignmentX(Component.CENTER_ALIGNMENT);
      GameScreen.moveToFront(weaponImage);
      weapon.add(weaponImage);
      JLabel inventory = inventories[i];
      GameScreen.moveToFront(inventory);
      weapon.add(inventory);
      secondaryWeapons.add(weapon);
    }         
    
    
    JLabel totalScore = new JLabel("[" + currentScoreDisplayed + "]"); //label to display current total score
    totalScore.setFont(new Font("Times Roman", Font.PLAIN, 
        22 + (thisFrame.getWidth() * thisFrame.getHeight()) / (int)Math.pow(10, 6)));
    totalScore.setForeground(Color.WHITE);
    totalScore.setBounds(new Rectangle(new Point(thisFrame.getWidth() 
        - (int)totalScore.getPreferredSize().getWidth() - (int)totalScore.getPreferredSize().getHeight(),
        thisFrame.getHeight() - 2 * (int)totalScore.getPreferredSize().getHeight()), totalScore.getPreferredSize()));
    GameScreen.add(totalScore);
    GameScreen.moveToFront(totalScore);
    
    
    //primary game function timers below------V   

    
    TimerTask spawnEnemy = new TimerTask() {
      public void run() {
        if (gameActive && !isPaused) {
          if ((int)(Math.random() * 10) < 3) { //chance per second to spawn an enemy DEFAULT = 3

            if ((int)(Math.random() * 10) < 8) { //single enemy
              int enemyNumber; //which enemy will be created
              int randomEnemyRange = (int)(Math.random() * 1001); //determines the chance of each type from being initialized
              if (randomEnemyRange < 350)  //small basic enemy
                enemyNumber = 1;
              else if (randomEnemyRange >= 350 && randomEnemyRange < 500) //large enemy
                enemyNumber = 2;
              else if (randomEnemyRange >= 500 && randomEnemyRange < 700) //medium enemy
                enemyNumber = 3;
              else if (randomEnemyRange >= 700 && randomEnemyRange < 800) //single instant laser1 enemy
                enemyNumber = 4;
              else if (randomEnemyRange >= 800 && randomEnemyRange < 920) //double instant laser1 enemy
                enemyNumber = 5;
              else 
                enemyNumber = 6; //spread weapon enemy

              enemies.add(new Enemy((int)(Math.random() * (GameScreen.getWidth() - 50)), -100, enemyNumber)); //spawn new enemy and add it to the list
            }
            else {
              new Formation((int)(Math.random() * 5) + 1); //unique formation of enemies
            }
          }
        }
      }                         
    };
    enemySpawnTickTimer = new Timer("enemySpawnTickTimer");
    enemySpawnTickTimer.scheduleAtFixedRate(spawnEnemy, 0, 400);



    TimerTask spawnDrop = new TimerTask() {
      public void run() {
        if(gameActive && !isPaused) {
          if((int)(Math.random() * 12) < 1) { //every second, 10% chance to spawn a drop
            String dropType;

            int x = (int)(Math.random() * 1001);     

            if(x < 250) 
              dropType = "GAIN_ENERGY";
            else if(x >= 250 && x < 550) 
              dropType = "COOLDOWN";
            else if(x >= 550 && x < 700) 
              dropType = "HEALTH";
            else if(x >= 700 && x < 800) 
              dropType = "INVINCIBILITY";
            else if(x >= 800 && x < 900) 
              dropType = "RAPID_FIRE";
            else 
              dropType = "DISABLE_ENEMY";

            powerups.add(new Drop(dropType, (int)(Math.random() * (GameScreen.getWidth() - 50)), -50));
          }
        }
      }
    };
    spawnDropTickTimer = new Timer("spawnDropTickTimer");
    spawnDropTickTimer.scheduleAtFixedRate(spawnDrop, 0, 1000); //every second, check to see if a drop will spawn



    TimerTask spawnWeapon = new TimerTask() {
      public void run() {
        if(gameActive && !isPaused) {
          if((int)(Math.random() * 10) < 1) {
            String weaponType = null;

            int x = (int)(Math.random() * 1001); 

            if(x < 350) 
              weaponType = "ROCKETS";
            else if(x >= 350 && x < 600)
              weaponType = "SMART_ROCKETS";
            else if(x >= 600 && x < 750)
              weaponType = "MACHINE_BLAST";
            else if(x >= 750 && x < 900)
              weaponType = "LASER";
            else
              weaponType = "SPREAD_BLAST";

            new Drop(weaponType, (int)(Math.random() * (GameScreen.getWidth() - 50)), -50);                     
          }                                     
        }
      }          
    };
    spawnWeaponTickTimer = new Timer("spawnWeaponTickTimer");
    spawnWeaponTickTimer.scheduleAtFixedRate(spawnWeapon, 0, 1000); //every second, check to see if a weapon should spawn



    TimerTask reduceHeat = new TimerTask() {
      public void run() {
        if (gameActive && !isPaused) {
          if (currentHeatState >= 100) { //once 100 heat units is hit, cool down heat rapidly
            isOverheated = true;
          }
          if (currentHeatState > 0) { //if the ship is heated, cool by 1
            currentHeatState--;
            heatBar.updateHeatBar();
          }
          if (isOverheated) { //state when the ship is over heated
            ship.setImage("Images/shipImageOverheated.png"); //when over heated or not, the ship will be a certain appearance
            if (currentHeatState <= 0) {
              ship.setImage("Images/spaceShip.png");
              isOverheated = false;
            }
          } 
        }     
      }
    };
    cooldownTimer = new Timer("cooldownTimer");
    cooldownTimer.scheduleAtFixedRate(reduceHeat, 0, 65);       

    

    TimerTask depleteEnergy = new TimerTask() {
      public void run() {
        if (gameActive && !isPaused) { //while game is active, count down
          currentEnergyState--;
          fuelBar.updateFuelBar();
          if (currentEnergyState <= 0) { //if you run out of fuel
            deathScreen("Images/deathLogoFuel.png"); //death when player runs out of fuel
          }
        }
      }
    };
    depleteEnergyTimer = new Timer("depleteEnergyTimer");
    depleteEnergyTimer.scheduleAtFixedRate(depleteEnergy, 1000, 1000); //every second, a unit of energy is depleted
    
    
    
    TimerTask increaseScore = new TimerTask() { //increases the score displayed for visual effect
      public void run() {
        if (currentScoreDisplayed < currentScore) {
          currentScoreDisplayed++;
          totalScore.setText("[" + currentScoreDisplayed + "]"); //re-orient bounds so that text doesn't run past screen
          totalScore.setBounds(new Rectangle(new Point(thisFrame.getWidth() 
              - (int)totalScore.getPreferredSize().getWidth() - (int)totalScore.getPreferredSize().getHeight(),
              thisFrame.getHeight() - 2 * (int)totalScore.getPreferredSize().getHeight()), totalScore.getPreferredSize()));
        }
      }
    };
    scoreIncreaseTimer = new Timer("scoreIncreaseTimer");
    scoreIncreaseTimer.scheduleAtFixedRate(increaseScore, 0, 5);
    
    
    
    GameScreen.addMouseListener(new MouseAdapter() { //for mouse INPUT events
      public void mouseEntered(MouseEvent e) { //when the mouse enters the game field
        if(!isPaused)
          ship.updateShipPos();

        GameScreen.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
      }
      public void mouseExited(MouseEvent e) {
        GameScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) { //left mouse button to shoot, hold down to fire automatically
          shootingTimer = new Timer("shootingTimer");
          TimerTask shootConstantly = new TimerTask() { //timer block put inside the mouse listener method so that the task and timer objects are recreated with each click
            public void run() {
              if(gameActive && !isPaused) {
                if(!isOverheated && rapid) { //if the ship isn't over heated, shoot always, but if rapid, wait and shoot a second bullet
                  ship.shoot(false, "main"); //false shooting parameter indicates not to omit the heat variable (AKA, increase heat)

                  //when rapid shot power up is collected, an extra bullet is fired in between each normal shot
                  new Timer().schedule(new TimerTask() { //schedules for only one execution
                    public void run() {
                      ship.shoot(true, "main"); //true means ignore the heat output for this extra bullet when shooting rapidly
                    }                                  
                  }, 85); //half time delay for second bullet 

                }
                else if(!isOverheated) { //otherwise if the ship can shoot but no rapid shot, shoot normally
                  ship.shoot(false, "main");
                }
              } 
            }
          };
          shootingTimer.scheduleAtFixedRate(shootConstantly, 0, 170);

        }
        else if(e.getButton() == MouseEvent.BUTTON3 && !isPaused) { //switch weapons using right mouse button (3rd button)
          if(ship.CurrentShipWeapon.equals("Regular Bullets")) { //primary weapon to secondary
            ship.CurrentShipWeapon = "Secondary Bullets";
          }
          else if(ship.CurrentShipWeapon.equals("Secondary Bullets")) { //secondary weapon to tertiary
            ship.CurrentShipWeapon = "Tertiary Bullets";
          }
          else if(ship.CurrentShipWeapon.equals("Tertiary Bullets")) { //tertiary weapon to primary
            ship.CurrentShipWeapon = "Regular Bullets";
          }
        }
        else if(e.getButton() == MouseEvent.BUTTON2) { //middle mouse wheel button shoots special weapons
          if(gameActive && !isPaused)
            ship.shoot(true, "special");
        }
      }
      public void mouseReleased(MouseEvent e) { //if you release the mouse button the shoot constantly timer is canceled and you cease firing
        try {
          shootingTimer.cancel(); //stop firing while firing automatically
        }
        catch(NullPointerException error) {
        }
      }
    });

    GameScreen.addMouseMotionListener(new MouseAdapter() { //for mouse MOVEMENT events

      public void mouseMoved(MouseEvent e) {
        if(!isPaused)
          ship.updateShipPos();
      }
      public void mouseDragged(MouseEvent e) {
        if(!isPaused)
          ship.updateShipPos();
      }

    });

    thisFrame.addMouseWheelListener(new MouseAdapter() { //for selecting secondary weapons
      public void mouseWheelMoved(MouseWheelEvent e) {
        if(!isPaused) {
          e.consume(); //prevents wheel listener from triggering twice
          if(e.getWheelRotation() > 0) { //rotated down
            weaponSelection--;
          }
          else if(e.getWheelRotation() < 0) { //rotated up
            weaponSelection++;          
          }
          updateWeaponSelection();
        }
      }
    });
  }


  public static void deathScreen(String splash) {

    //shut down game play
    gameMusic.getClip().stop();
    dead = true;
    gameActive = false; 
    GameScreen.removeAll();
    thisFrame.remove(GameScreen);
    enemySpawnTickTimer.cancel(); //cancel all "game function" timers
    spawnDropTickTimer.cancel();
    spawnWeaponTickTimer.cancel();
    cooldownTimer.cancel();
    depleteEnergyTimer.cancel(); 
    scoreIncreaseTimer.cancel();
    timeLeft = 0; //safety net to also cancel all effects
    for(Enemy enemy: enemies) { //kill all enemies
      enemy.isDead = true;
    }
    try {
      shootingTimer.cancel(); //prevents restarted games from having shooting timer stacking together
    }
    catch(NullPointerException e) {                 
    }

    JPanel deathPanel = new JPanel();
    deathPanel.setLayout(new BoxLayout(deathPanel, BoxLayout.Y_AXIS));
    deathPanel.setBackground(new Color(100,0,0));

    JLabel deathLabel = new JLabel(new ImageIcon(splash));
    deathLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    deathPanel.add(deathLabel);   

    JLabel highscoreLabel = new JLabel("-[CURRENT HIGH SCORE]-");
    deathPanel.add(highscoreLabel);
    highscoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    highscoreLabel.setForeground(Color.WHITE);
    highscoreLabel.setFont(new Font("TimesRoman", Font.PLAIN, 40));

    if (currentScore > currentHighscore) { //will record and save the current high score
      currentHighscore = currentScore;
    }

    JLabel currentHighscoreLabel = new JLabel(Integer.toString(currentHighscore));
    deathPanel.add(currentHighscoreLabel);
    currentHighscoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    currentHighscoreLabel.setForeground(Color.YELLOW);
    currentHighscoreLabel.setFont(new Font("TimesRoman", Font.PLAIN, 25));  

    deathPanel.add(new Label());

    JLabel yourScore = new JLabel("[Mission Score]: " + currentScore);
    deathPanel.add(yourScore);
    yourScore.setAlignmentX(Component.CENTER_ALIGNMENT);
    yourScore.setForeground(new Color(50, 140, 255));
    yourScore.setFont(new Font("TimesRoman", Font.PLAIN, 20));         

    JLabel accuracyLabel = new JLabel("[Bullet Accuracy]: "
        + Double.toString(Math.round(100 * (shotsImpacted / shotsFired))) + "%"); 
    deathPanel.add(accuracyLabel);
    accuracyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    accuracyLabel.setForeground(new Color(50, 140, 255));
    accuracyLabel.setFont(new Font("TimesRoman", Font.PLAIN, 20));       

    deathPanel.add(new Label());

    JLabel returnLabel = new JLabel(">>  [RETURN TO MENU]  <<");
    deathPanel.add(returnLabel);
    returnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    returnLabel.setForeground(Color.WHITE);
    returnLabel.setFont(new Font("TimesRoman", Font.PLAIN, 30));       
    thisFrame.add(deathPanel);  //add all of the death screen components    

    deathPanel.add(new Label());
    deathPanel.add(new Label());

    new Sound("Sounds/GameOver.wav"); //play game over sound
    returnLabel.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {

        currentScore = 0; //reset everything
        currentScoreDisplayed = 0;
        currentHeatState = 0;
        currentEnergyState = 100; 
        energyColorDeterminant = (int)((255.0 / 100.0) * (double)currentEnergyState); //for energy bar color change
        introSizeChange = 0;
        shotsFired = 0;
        shotsImpacted = 0;
        loadProgression = 0;
        durationBarLengthRatio = 0.0; //default coefficient of 0 when no power is active

        healthBarColor = new Color(150, 0, 0); //reset colors of health, heat, and fuel bars
        heatBarColor = new Color(255, 255, 0);
        fuelBarColor = new Color(0, 255, 0);

        weaponSelection = 0; //reset secondary weapon inventory
        rocketCount = 0;
        smartRocketCount = 0;
        laserCount = 0;
        machineBlasterCount = 0;
        spreadBlastCount = 0;

        ship.CurrentShipWeapon = "Regular Bullets"; //reset status of ship weapon
        rapid = false; //also causes effect timers to cancel too
        invincible = false;
        enemyFireDisabled = false;
        powerups.clear();
        enemies.clear(); //clear enemies array
        dead = false; //can now take damage again
        thisFrame.remove(deathPanel);

        new Sound("Sounds/clickSound.wav");
        setupMenu(true, true);
      }
      public void mouseEntered(MouseEvent e) {               
        returnLabel.setForeground(new Color(130, 130, 130));
        deathPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        new Sound("Sounds/mouseOverSound.wav");
      }
      public void mouseExited(MouseEvent e) {
        returnLabel.setForeground(Color.WHITE);
        deathPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    });           

  }


  //information update methods below
  public static void updateWeaponSelection() {
    if (weaponSelection < 0) { //if selection goes past rockets to the left
      weaponSelection = 4; //back to weapon choice at spread blast      
    } else if (weaponSelection > 4) { //if selection goes past spread blast to the right
      weaponSelection = 0; //back to weapon choice at rockets
    }
    
    for (int i = 0; i < inventories.length; i++) {
      inventories[i].setForeground(Color.WHITE);
    }
    inventories[weaponSelection].setForeground(Color.RED);
  }
  
  public static void updateDropCountdown(String path) { 
    ImageIcon image = new ImageIcon(path);
    Image scaledImage = image.getImage().getScaledInstance(image.getIconWidth() + thisFrame.getWidth() / 150,
        image.getIconHeight() + thisFrame.getWidth() / 150, java.awt.Image.SCALE_SMOOTH);
    currentEffect.setIcon(new ImageIcon(scaledImage)); //set image to either a power up or blank
    durationBar.repaint();
  }



  public static class graphicsComponent extends JPanel { //graphicsComponent "inherits" JPanel properties

    private String type;
    public int xCorner;
    public int yCorner;
    public int width;
    public int height;

    public graphicsComponent(String type, int xCorner, int yCorner, int width, int height) {
      this.type = type;
      this.xCorner = xCorner;
      this.yCorner = yCorner;
      this.width = width;
      this.height = height;

    }

    /** creates the graphical component.
     * 
     */
    public void paintComponent(Graphics g) { 
      super.paintComponent(g);
      setOpaque(false); //keep the graphical "canvas" invisible
      g.drawRect(xCorner, yCorner, width, height);

      if (type.equals("loadingBar")) { //loading bar
        g.setColor(new Color(255, 255 - (loadProgression / 2), 255 - loadProgression)); //color of bar
        g.fillRect(xCorner, yCorner, loadProgression, height);
      } else if (type.equals("fuelBar")) { //health, heat, and energy meter lengths will be relative to screen size
        g.setColor(fuelBarColor);
        g.fillRect(xCorner, yCorner, (int)((double)width * ((double)currentEnergyState / 100.0)), height);
      } else if (type.equals("heatBar")) { 
        g.setColor(heatBarColor);
        g.fillRect(xCorner, yCorner, (int)((double)width * ((double)currentHeatState / 100.0)), height);
      } else if (type.equals("healthBar")) { 
        g.setColor(healthBarColor);
        g.fillRect(xCorner, yCorner, (int)((double)width * ((double)ship.health / 1000.0)), height);
      } else if (type.equals("durationBar")) { //bar that displays remaining time of a power up effect
        g.setColor(new Color(0, 255, 255)); //"static" light blue color
        g.fillRect(xCorner, yCorner, (int)((double)width * durationBarLengthRatio), height);
        //duration bar length ratio is the ratio of the time left of a power up and the time it lasts
        //determines the length of the bar, which is updated per second a power is active/activated
      }

      repaint(); //repaint the backdrop of the meter/bar after optimizations. This ensures no background clipping/issues
    }


    public void load() { //increase the bars fill + change color increment between 20 - 30 units
      if (loadProgression + 1 >= 250) { //before bar overflows, end loading timer and start game
        loadProgression = 250; //if the bar is full, start the game
        loadTimer.cancel();
      } else {
        loadProgression += 1;
      }
      repaint(); //redraw the loading bar
    }

    public void updateHealthBar() { //method only applies to heat bar type
      healthBarColor = Color.RED; //change bar to lighter red color
      repaint(); //update health bar length      
      new Timer().scheduleAtFixedRate(new TimerTask() {
        public void run() {
          if (healthBarColor.getRed() <= 150) { //if the health bar color is less than or equal to default red
            this.cancel();
          } else {
            healthBarColor = new Color(healthBarColor.getRed() - 1, 0, 0); //fade into original dark red color
            repaint(); //set new color
          }
        }
      }, 0, 15);
    }

    public void updateHeatBar() { //method only applies to heat bar type
      heatBarColor = new Color(255, 255 - (2 * currentHeatState), 0); //Green: 255 is base green value
      repaint();
    }

    public void updateFuelBar() { //method only applies to fuel bar type
      energyColorDeterminant = (int)((255.0 / 100.0) * (double)currentEnergyState);
      fuelBarColor = new Color(255 - energyColorDeterminant, energyColorDeterminant, 0).brighter(); 
      repaint();
    }

  }



  public static class Ship {

    public int health;
    public int x, y;
    public String CurrentShipWeapon;

    public Ship(int xLocation, int yLocation, int health, String weapon) {
      //references aspects of the current ship object
      this.y = yLocation;
      this.health = health;
      this.CurrentShipWeapon = weapon;

      shipImage = new JLabel(new ImageIcon("Images/spaceShip.png"));
      this.x = xLocation - (shipImage.getWidth() / 2);

      shipImage.setLayout(null);
      shipImage.setBounds(new Rectangle(new Point(x, y), shipImage.getPreferredSize()));
      GameScreen.add(shipImage);
    }

    public void updateShipPos() {
      int newPosX = (int)MouseInfo.getPointerInfo().getLocation().getX() - (shipImage.getWidth() / 2);
      int newPosY = (int)MouseInfo.getPointerInfo().getLocation().getY();
      shipImage.setBounds(new Rectangle(new Point(newPosX, newPosY), shipImage.getPreferredSize()));
      this.x = newPosX; //update location and object variables
      this.y = newPosY;
    }

    public Rectangle getShipHitbox() { //player hit box access method for classes outside ship
      return shipImage.getBounds();
    }

    public void damage(int dmg) { //when player ship is damaged
      if ((invincible && dmg > 0) && !dead) { //if you're shielded and you're taking damage/not healing (dmg > 0), or you've already been defeated
        new Sound("Sounds/shieldDamaged.wav");
        return; //damage is ineffective, no health check/update
      } else if ((!invincible && dmg > 0) && !dead) { //else if not invincible and not healing/taking damage, deal damage and play sound, otherwise don't
        this.health -= dmg; //TAKE DAMAGE
        new Sound("Sounds/Damaged.wav");
      } else if (dmg < 0 && !dead) { //else if healing and not dead, heal and don't play sound
        this.health -= dmg; //HEAL
      } else if (dead) { //if your ship is dead don't do anything
        return;
      }

      if (this.health > 1000) { //if the gain in health from healing goes over 1000 total health, reduce back to a cap of 1000
        this.health = 1000;
      }

      healthBar.updateHealthBar(); //after ship health value is updated, update the graphical health bar too
      if (health <= 0) {             
        deathScreen("Images/deathLogoHealth.png"); //when the player runs out of health, the death screen will indicate ship defeat
      }
    }

    public void shoot(boolean omitHeat, String weaponClass) { //needs a weapon class determinant since main weapon and secondary weapons are independent Strings
      if (weaponClass.equals("main")) { //if the weapon is a main weapon
        new Bullet(CurrentShipWeapon, weaponClass, omitHeat, 0, 0); //shoot new bullet and add it to the list
        if(CurrentShipWeapon.equals("Tertiary Bullets")) {
          new Bullet(CurrentShipWeapon, weaponClass, omitHeat, shipImage.getWidth(), 0); //if the third selected weapon, shoot another side bullet
        }
        //sounds played upon a single click instead of in constructor to prevent lag as well as sound echo from weapon 3's double shot
        if (CurrentShipWeapon.equals("Regular Bullets")) {
          new Sound("Sounds/regBulletSound.wav");
        } else if (CurrentShipWeapon.equals("Secondary Bullets")) {
          new Sound("Sounds/secondaryBulletSound.wav");
        } else if (CurrentShipWeapon.equals("Tertiary Bullets")) {
          new Sound("Sounds/tertBulletSound.wav");
        }
      }
      else if (weaponClass.equals("special")) { //if the weapon is a secondary special weapon of the following possible types
        if (weaponImagePaths[weaponSelection].equals("Images/rocketWeapon.png") && rocketCount > 0) { 
          new Bullet("rockets", weaponClass, true, 0, 0);
          new Bullet("rockets", weaponClass, true, shipImage.getWidth(), 0);
          rocketCount--;
          rockets.setText("[" + rocketCount + "]");
          new Sound("Sounds/missileWeaponSound.wav");
        } else if (weaponImagePaths[weaponSelection].equals("Images/laserWeapon.png") && laserCount > 0) {
          new Bullet("lasers", weaponClass, true, 0, 0);
          laserCount--;
          lasers.setText("[" + laserCount + "]");
          new Sound("Sounds/laserWeaponSound.wav");
        } else if (weaponImagePaths[weaponSelection].equals("Images/smartRocketWeapon.png") && smartRocketCount > 0) {
          new Bullet("smartRockets", weaponClass, true, 0, 0);
          new Bullet("smartRockets", weaponClass, true, shipImage.getWidth(), 0);
          smartRocketCount--;
          smartRockets.setText("[" + smartRocketCount + "]");
          new Sound("Sounds/missileWeaponSound.wav");
        } else if (weaponImagePaths[weaponSelection].equals("Images/machineBlastWeapon.png") && machineBlasterCount > 0) {
          for (int i = 0; i < 8; i++) { //each of the 8 shots can be between the whole width of the ship, and a third the height of the ship in front as shown below
            new Bullet("machineBlast", weaponClass, true, (int)(Math.random() * shipImage.getWidth()), (int)(Math.random() * (shipImage.getHeight() / 3))); 
          }
          machineBlasterCount--;
          machineBlaster.setText("[" + machineBlasterCount + "]");
          new Sound("Sounds/machineBlastWeaponSound.wav");
        } else if (weaponImagePaths[weaponSelection].equals("Images/spreadBlastWeapon.png") && spreadBlastCount > 0) {
          new Bullet("spreadBlast", weaponClass, true, 0, 20); //one center straight bullet
          for (int i = 0; i < 4; i++) { //4 pairs bullets evenly spread like a shotgun to total 9 bullets. All move up at the same speed
            new Bullet("spreadBlast", weaponClass, true, i + 1, 0); //on each side of the spread burst, each bullet moves at one tick rate more/less than the one next to it
            new Bullet("spreadBlast", weaponClass, true, -(i + 1), 0); 
          }
          spreadBlastCount--;
          spreadBlast.setText("[" + spreadBlastCount + "]");
          new Sound("Sounds/spreadBlastWeaponSound.wav");
        }
      }
    }

    public void setImage(String path) {
      shipImage.setIcon(new ImageIcon(path));
    }

  }



  public static class Bullet {

    private int damage, thermalOutput;
    private int x, y, ySpeed = 1; //by default the movement rate is 1
    private int laserClock = 0; //time elapsed for laser weapons
    private JLabel shotBullet;
    private Timer myTimer, lockOn; //lock on timer replaces the previous myTimer for straight movement in order to track the enemy in a diagonal direction
    private String weaponClass, type; //class refers to main or special to keep track of no. of impacts, type is the specific weapon in that class for everything else
    private boolean lockedOn = false;

    public Bullet(String type, String weaponClass, boolean omitHeat, int changeX, int changeY) { //when a bullet is initialized, its parameters are constructed, it is displayed, and it is moved
      this.weaponClass = weaponClass;
      this.type = type;

      if (type.equals("Regular Bullets")) { //statistics and images of the two types of bullets constructed here, then they are displayed
        this.damage = 45;
        this.thermalOutput = 5;
        shotBullet = new JLabel(new ImageIcon("Images/regBullet.png"));
        shotsFired++; //increase the number of shots fired by 1 per individual bullet
      } else if (type.equals("Secondary Bullets")) {
        this.damage = 35;
        this.thermalOutput = 4;
        shotBullet = new JLabel(new ImageIcon("Images/secondaryBullet.png"));
        shotsFired++;
      } else if (type.equals("Tertiary Bullets")) {
        this.damage = 20; 
        this.thermalOutput = 2; //4 total with two bullets
        shotBullet = new JLabel(new ImageIcon("Images/tertBullet.png"));
        shotsFired++;
      } else if (type.equals("rockets")) { //special weapons
        this.damage = 75; 
        shotBullet = new JLabel(new ImageIcon("Images/rocketWeaponBullet.png"));
      } else if (type.equals("lasers")) {
        this.damage = 500; //Guaranteed kill
        shotBullet = new JLabel(new ImageIcon("Images/laserWeaponBullet.png"));
        ySpeed = 0; //no movement
      } else if (type.equals("smartRockets")) {
        this.damage = 55; 
        shotBullet = new JLabel(new ImageIcon("Images/smartRocketWeaponBullet.png"));
      } else if (type.equals("machineBlast")) {                
        this.damage = 15; 
        shotBullet = new JLabel(new ImageIcon("Images/machineBlastWeaponBullet.png"));
      } else if (type.equals("spreadBlast")) {
        this.damage = 30;
        shotBullet = new JLabel(new ImageIcon("Images/spreadBlastWeaponBullet.png"));
      }

      if (omitHeat || type.equals("special")) {//no heat change
        this.thermalOutput = 0;                
      }

      if (!type.equals("Tertiary Bullets") && !type.equals("rockets") && !type.equals("smartRockets")) { //if not a dual bullet weapon
        if (type.equals("machineBlast")) {
          this.x = ship.x + changeX; //passed with random values creating a bullet "mass" effect
          this.y = ship.y + changeY;
        } else if (type.equals("lasers")) {
          this.x = (ship.x + (shipImage.getWidth() / 2)) - ((int)shotBullet.getPreferredSize().getWidth() / 2);
          this.y = ship.y - ((int)shotBullet.getPreferredSize().getHeight()); //bottom of laser is at the tip of the ship
        } else { //if anything else (main weapon 1 or 2 or yellow spread blast)
          this.x = (ship.x + (shipImage.getWidth() / 2))
              - ((int)shotBullet.getPreferredSize().getWidth() / 2); //spawns initially at the middle of the tip of the ship
          this.y = ship.y - ((int)shotBullet.getPreferredSize().getHeight() / 2);
        }
      } else { //if double shot weapon 3, rockets, or smart rockets, basically anything that is a pair
        if (changeX > 0) { 
          this.x = (ship.x + shipImage.getWidth())
              - (int)shotBullet.getPreferredSize().getWidth(); //at right side, top right corner oriented
        } else {
          this.x = ship.x; //at left side
        }
        this.y = ship.y + (shipImage.getWidth() / 3); //both around the front of the ship wings for visual purposes       
      }

      shotBullet.setLayout(null);
      shotBullet.setBounds(new Rectangle(new Point(x, y), shotBullet.getPreferredSize()));

      try {
        GameScreen.add(shotBullet); //load in bullet image without passing illegal component positions
        GameScreen.moveToFront(shotBullet);  //move bullets to the very top of the current container  
      }
      catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e) { 
      }
      currentHeatState += thermalOutput; //update heat level out of 100 units
      heatBar.updateHeatBar(); //update heat meter bar

      TimerTask moving = new TimerTask() { //timer task started for each bullet shot
        public void run() {
          if (y < -shotBullet.getHeight() && !type.equals("lasers")) { //when the bullet is past the top of the screen
            remove(); //remove bullet from screen
          } else {
            if (type.equals("smartRockets")) { //if the weapon is a seeking weapon (smart rockets)
              if (!lockedOn) {
                for (Enemy enemy: enemies) {
                  Enemy reference = enemy; //avoid concurrent modification exception
                  //center of the bullet
                  int centerX = (int)(shotBullet.getLocation().getX() + (shotBullet.getWidth() / 2));
                  int centerY = (int)(shotBullet.getLocation().getY() + (shotBullet.getHeight() / 2));
                  //if the distance is close enough
                  int dX = (int)(Math.abs(reference.getCenter().getX() - centerX));
                  int dY = (int)(Math.abs(reference.getCenter().getY() - centerY));
                  //if the X distance between the enemy and rocket are <= 150 AND the rocket is below/moving towards the enemy, lock on
                  if ((dX < 150 && dY < 150) && centerY > enemy.getCenter().getY()) { 

                    //if the center of the straight moving smart rocket is in between the very left and very right 
                    //of the enemy hit box, continue on a straight path (no change in x) since it is a guaranteed impact
                    if (centerX >= (reference.getCenter().getX() - (reference.getHitbox().getWidth() / 2))
                        && centerX <= (reference.getCenter().getX() + (reference.getHitbox().getWidth() / 2))) {
                      dX = 0; 
                    } else if ((int)(dX / 100) < 1) { //otherwise move diagonally so that the rockets reach the enemy
                      dX = 100;
                      dY = 200; //move faster to hit target without missing
                    }
                    //only if the enemy is to the right of the rocket, reverse x movement so that the rocket moves to the right
                    if (reference.getCenter().getX() > centerX) {
                      dX *= -1; 
                    }
                    lockedOn = true; //no longer search for target
                    seek((int)(dX / 100), (int)(dY / 100)); //initiate homing when locked on
                  }
                }
                updateBulletY(ySpeed); //normal straight path before possibly locking onto something
              }
            } else {
              updateBulletY(ySpeed); //default 1 as set above the constructor
            }
            for (int i = 0; i < enemies.size(); i++) { //iterate through all enemy ships on screen to check if they intersect after movement change
              try { //prevent code from throwing error if an enemy outside the range of the array is collision checked
                doesCollide(enemies.get(i));       
              }
              catch(IndexOutOfBoundsException e) {
              }
            }
          }         
        }
      };
      myTimer = new Timer("myTimer");
      myTimer.scheduleAtFixedRate(moving, 0, 1);


      if (type.equals("lasers")) { //the laser will last for a brief period motionless, before disappearing          
        TimerTask prolong = new TimerTask() {
          public void run() {
            if (laserClock >= 3) {
              remove();
            } else {
              laserClock++;
            }
          }
        };
        Timer myTimer = new Timer("myTimer");
        myTimer.scheduleAtFixedRate(prolong, 100, 100);
      }

      if (type.equals("spreadBlast")) { //for the spread blast weapon to spread in the x direction as well
        TimerTask spread = new TimerTask() {
          public void run() {
            updateBulletX(changeX); //spread distance based on the x adjust value to ensure it moves at an angle
          }
        };
        Timer myTimer = new Timer("myTimer");
        myTimer.scheduleAtFixedRate(spread, 0, 8); //can change tick period to change spread range 
      }

    }        



    public void updateBulletY(int ySpeed) {
      if (!isPaused) {
        this.y -= ySpeed; //update the bullet's y position to keep track of collision
        shotBullet.setLayout(null);
        shotBullet.setBounds(new Rectangle(new Point(x, y), shotBullet.getPreferredSize()));
      }
    }

    public void updateBulletX(int xSpeed) {
      if (!isPaused) {
        this.x -= xSpeed; //update the bullet's y position to keep track of collision
        shotBullet.setLayout(null);
        shotBullet.setBounds(new Rectangle(new Point(x, y), shotBullet.getPreferredSize()));
      }
    }

    public void seek(int rateX, int rateY) { //moves the rocket in a diagonal manner when locked on
      TimerTask follow = new TimerTask() {
        public void run() {
          updateBulletX(rateX);
          updateBulletY(rateY);
        }
      };
      lockOn = new Timer("myTimer");
      lockOn.scheduleAtFixedRate(follow, 0, 1);
    }

    public void remove() {
      shotBullet.setVisible(false); //remove from screen
      GameScreen.remove(shotBullet);
      myTimer.cancel(); //prevent bullet bounds from moving and continuing to damage enemy ships
    }

    public void doesCollide(Enemy enemy) { //detects a collision if the hit box of a ship is intersected with that of a bullet
      try { //catch null exception if program tries to access a null hit box
        if (shotBullet.getBounds().intersects(enemy.getHitbox())) {
          enemy.damageEnemy(damage); //no HUD update because the HUD does not keep track of enemy health unlike player health
          enemy.checkForDeath(); //detect if the collided ship has lost all health
          if (!type.equals("lasers")) {//if the special attack isn't a laser, it will be removed on impact. Otherwise, the laser will linger with it's prolong timer                            
            remove(); 
          }
          if (weaponClass.equals("main")) {//if not a special attack, it counts as an "impacted" shot and is counted
            shotsImpacted++; //increases the number of bullets that have dealt damage successfully 
          }
        }
      }
      catch(NullPointerException e) {                
      }
    }

  }                    



  public static class Enemy { //complex class that handles movement and shooting patterns of different ships

    private int health, x, y, enemyTypeNumber, animationFrameValue = 1; //keeps track of each animation frame
    private int impactDamage, projectileDamage, scale; 
    private Timer myTimer;      
    private JLabel enemyImage, animationFrame; //animationFrame for the actual JLabel of the animation image
    public boolean isDead = false, killByFire = true; //kill by fire default true unless played ship collides with enemy ship
    public ArrayList<enemyProjectile> lasers = new ArrayList<enemyProjectile>(); //for bullets (lasers specifically) that linger and must be kept record of to be removed

    //here are the data types for enemy ships with unique qualities
    private double waitTime = 0; //for ships that have unique or non-continuous movement patterns
    private int shotCount = 0; //specifically for if the ship type can shoot multiple times
    private Timer wait, continueOn; //movement restart method for ships that stop moving or slow down
    private Timer Enemy3ShotTimer;
    

    public Enemy(int xPos, int yPos, int enemyType) { //enemies will generate at fixed y coordinates, but random x locations, and health
      this.x = xPos;
      this.y = yPos;
      this.enemyTypeNumber = enemyType;
      this.impactDamage = ((int)(Math.random() * 11) + 70); //70 to 80 impact damage from a collision

      if (enemyTypeNumber == 1) {
        enemyImage = new JLabel(new ImageIcon("Images/enemy1.png")); //standard enemy
        health = (int)(Math.random() * 41) + 50; //minimum health of 500, max health of 90
        projectileDamage = 55; //single medium damage shot
        scale = 0;
      } else if (enemyTypeNumber == 2) {
        enemyImage = new JLabel(new ImageIcon("Images/enemy2.png")); //large enemy
        health = (int)(Math.random() * 41) + 120; //minimum health of 120, max health of 160
        projectileDamage = 70; //double shot large damage
        scale = 45;
      } else if (enemyTypeNumber == 3) {
        enemyImage = new JLabel(new ImageIcon("Images/enemy3.png")); //medium enemy
        health = (int)(Math.random() * 41) + 80; //minimum health of 80, max health of 120
        projectileDamage = 35; //triple straight low damage
        scale = 35;
      } else if (enemyTypeNumber == 4) {
        enemyImage = new JLabel(new ImageIcon("Images/enemy4.png")); //laser enemy
        health = (int)(Math.random() * 41) + 180; //minimum health of 180, max health of 220
        projectileDamage = 150; //heavy instant damage in one laser
        scale = 65;
      } else if (enemyTypeNumber == 5) {
        enemyImage = new JLabel(new ImageIcon("Images/enemy5.png"));
        health = (int)(Math.random() * 41) + 220; //minimum health of 160, max health of 200
        projectileDamage = 90; //large instant damage over two lasers
        scale = 65;
      } else if (enemyTypeNumber == 6) {
        enemyImage = new JLabel(new ImageIcon("Images/enemy6.png"));
        health = (int)(Math.random() * 41) + 310; //minimum health of 310, max health of 350
        projectileDamage = 25;
        scale = 75;
      }

      enemyImage.setLayout(null); 
      enemyImage.setBounds(new Rectangle(new Point(x, y), enemyImage.getPreferredSize()));
      try {
        GameScreen.add(enemyImage);
        GameScreen.moveToFront(enemyImage);
      }
      catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
      }

      GameScreen.addMouseMotionListener(new MouseAdapter() { //usage of this mouse motion listener is for ships that stop moving for a period of time, since updateEnemy() is not invoked while motionless
        public void mouseMoved(MouseEvent e) {
          if (getHitbox().intersects(ship.getShipHitbox()) && !isDead) { //if the ship runs into this enemy while it is alive and motionless
            if (enemyTypeNumber == 4 || enemyTypeNumber == 5 || enemyTypeNumber == 6) { //for the enemy types that have an idle
              killByFire = false; //ships collided, so don't give or display score/points
              remove();
              explode(0, scale);
              new Sound("Sounds/explosionSound.wav"); //play explosion sound
              ship.damage(impactDamage); 
            }
          }
        }
      });


      TimerTask moveEnemy = new TimerTask() {
        public void run() {                 
          updateEnemy();                 
          if (enemyTypeNumber == 4 && y == 100) { //single laser
            wait = new Timer("wait"); //for idling and carrying out ship abilities while motionless 
            continueOn = new Timer("continueOn"); //for "restarting" the canceled enemy movement timer
            myTimer.cancel(); //cancel primary movement timer
            TimerTask idle = new TimerTask() { //for waiting on ships when they have unique combat patterns
              public void run() {
                if (isDead) {
                  wait.cancel();
                }
                if (!isPaused) {
                  waitTime += 1;      
                }
                if (waitTime == 200 && !enemyFireDisabled && !isDead) {
                  lasers.add(new enemyProjectile(x, y, projectileDamage, 4, enemyImage.getWidth(), enemyImage.getHeight(), 0));                         
                }
                if (waitTime == 250 && !enemyFireDisabled && !isDead) {
                  try { 
                    for (enemyProjectile next: lasers) {
                      next.remove(true);
                    }
                  }
                  catch(NullPointerException e) {
                  }
                }
                if (waitTime == 350) {
                  if (!isDead) {
                  wait.cancel(); //stop the ship from idling, continue moving the ship forward
                    continueOn.scheduleAtFixedRate(new TimerTask() {
                      public void run() {
                        updateEnemy(); //now resumes movement via a different timer                                         
                      }
                    }, 0, 2);
                  }
                }
              }                             
            };
            wait.scheduleAtFixedRate(idle, 0, 10);
          } else if (enemyTypeNumber == 5 && y == 100) { //double laser
            wait = new Timer("wait");
            continueOn = new Timer("continueOn"); //for "restarting" the canceled enemy movement timer. Does the same thing
            myTimer.cancel();
            TimerTask idle = new TimerTask() { //for waiting on ships when they have unique combat patterns
              public void run() {
                if (isDead) {
                  wait.cancel();
                }
                if (!isPaused) {
                  waitTime += 1;                                
                }
                if (waitTime == 200 && !enemyFireDisabled && !isDead) {
                  lasers.add(new enemyProjectile(x, y, projectileDamage, 5, 0, enemyImage.getHeight(), 0));   
                  lasers.add(new enemyProjectile(x, y, projectileDamage, 5, enemyImage.getWidth(), enemyImage.getHeight(), 0)); 
                }
                if (waitTime == 250 && !enemyFireDisabled && !isDead) {
                  for (enemyProjectile next: lasers) {
                    next.remove(true);
                  }
                  projectileDamage = 30; //set damage for second weapon
                }
                if (waitTime > 350 && (waitTime % 50 == 5 || waitTime % 50 == 15) && !enemyFireDisabled && !isDead) {
                  new enemyProjectile(x, y, projectileDamage, 5.2, enemyImage.getWidth(), enemyImage.getHeight(), 0); 
                }
                if (waitTime == 520) {
                  if (!isDead) {
                  wait.cancel(); //stop the ship from idling, continue moving the ship forward
                    continueOn.scheduleAtFixedRate(new TimerTask() {
                      public void run() {
                        updateEnemy();                                              
                      }
                    }, 0, 2);
                  }
                }
              }                             
            };
            wait.scheduleAtFixedRate(idle, 0, 10);
          } else if (enemyTypeNumber == 6 && y == 100) { //rapid fire ship
            wait = new Timer("wait");
            continueOn = new Timer("continueOn"); 
            myTimer.cancel(); 
            TimerTask idle = new TimerTask() { 
              public void run() {
                if (isDead) {
                  wait.cancel();
                }
                if (!isPaused) {
                  waitTime += 1;      
                }
                if ((waitTime == 140 || waitTime == 180 || waitTime == 220) && !enemyFireDisabled && !isDead) {
                  for (int i = -2; i < 3; i++) {
                    new enemyProjectile(x, y, projectileDamage, 6, enemyImage.getWidth(), enemyImage.getHeight(), i);
                  }
                }
                if (waitTime == 300) {
                  if (!isDead) {
                    wait.cancel(); //stop the ship from idling, continue moving the ship forward
                      continueOn.scheduleAtFixedRate(new TimerTask() {
                        public void run() {
                          updateEnemy();                                              
                        }
                      }, 0, 2);
                    }
                  }
              }
            };
            wait.scheduleAtFixedRate(idle, 0, 10);
          } else if (y == (GameScreen.getHeight() / 2) - 300) { //when the enemy reaches a certain part of the screen it will fire once     
            if (enemyTypeNumber == 1 && !enemyFireDisabled && !isDead) {
              new enemyProjectile(x, y, projectileDamage, 1, enemyImage.getWidth(), enemyImage.getHeight(), 0);
            } else if (enemyTypeNumber == 2 && !enemyFireDisabled && !isDead) {
              new enemyProjectile(x, y, projectileDamage, 2, 0, enemyImage.getHeight(), 0);
              new enemyProjectile(x, y, projectileDamage, 2, enemyImage.getWidth(), enemyImage.getHeight(), 0);
            } else if (enemyTypeNumber == 3 && !enemyFireDisabled) {
              TimerTask separateShots = new TimerTask() {
                public void run() {
                  if (!isDead) {
                    new enemyProjectile(x, y, projectileDamage, 3, enemyImage.getWidth(), enemyImage.getHeight(), 0);
                  }
                  shotCount++;
                  if (shotCount == 3 || isDead) { //when enemy 3 fires three successive shots or is killed
                    Enemy3ShotTimer.cancel();
                  }
                }
              };
              Enemy3ShotTimer = new Timer("shoot");
              Enemy3ShotTimer.scheduleAtFixedRate(separateShots, 0, 100);
            }                           

          }
        }
      };
      myTimer = new Timer("myTimer");
      myTimer.scheduleAtFixedRate(moveEnemy, 0, 3);
    }


    public void updateEnemy() { //moves the ship, then checks for a collision or if it moves off screen
      if (!isPaused) {
        this.y += 1;
        enemyImage.setLayout(null);
        enemyImage.setBounds(new Rectangle(new Point(x,y), enemyImage.getPreferredSize()));
        //if the ship is hit by the enemy while MOVING. This method and damage check is never active when the enemy is idle
        if (getHitbox().intersects(ship.getShipHitbox())) { 
          killByFire = false; //indicates if the ship was shot or collided with and if points are alloted + displayed
          remove(); 
          explode(0, scale); //explode with no score since enemy was collided with and not shot
          new Sound("Sounds/explosionSound.wav"); 
          ship.damage(impactDamage); 
        }
        if (y > GameScreen.getHeight() + 50) { //past the bottom of the screen
          remove();    
        }
      }
    }

    public void remove() { //when removing an enemy from defeat, ship death, clearing, etc    
      isDead = true; //halt timers while running
      try { //if the lingering bullet has been shot but hasn't been removed already by the wait timer, remove it
        for (enemyProjectile next: lasers) {
          next.remove(true);        
        }
      }
      catch(NullPointerException e) { //to avoid non existent lasers from being removed if the ship has been killed before or after lasers have been fired
      }
      enemyImage.setVisible(false); //remove from screen
      GameScreen.remove(enemyImage);
      enemies.remove(this); //remove from ArrayList of enemies            
      myTimer.cancel(); //stop the enemy hit box from moving
      if (enemyTypeNumber == 4 || enemyTypeNumber == 5 || enemyTypeNumber == 6) { //if these aren't canceled they will result in instant death rather than a single damaging  
        try {
          continueOn.cancel(); //if the restart timer hasn't been canceled already for ships that stop before moving again
        }
        catch(NullPointerException e) {
        }
      }
    }

    //as usual, some methods, though redundant, are necessary for clarity and simplification of syntax
    public Rectangle getHitbox() {
      return enemyImage.getBounds();
    }

    public void damageEnemy(int dmg) { //must use helper method since bullet class can't access enemy health like the ship can access its health
      this.health -= dmg;
    }

    public Point getCenter() { //used to find the enemy's central point location for even distance homing
      return new Point((int)(enemyImage.getLocation().getX() + (getHitbox().getWidth() / 2)), 
          (int)(enemyImage.getLocation().getY() + (getHitbox().getHeight() / 2)));
    }

    public void checkForDeath() { //when an enemy ship dies, increase the score and destroy the enemy ship
      if (health <= 0 && !isDead) { //if the health drops below limit and enemy dies, gain score and possibly spawn gold star bonus
        int scoreToBeDisplayed = 0;
        isDead = true; //prevents multiple point counts/sound play-backs due to above boolean condition
        
        //no score update method necessary, as score increase timer updates display any time points are allotted
        if (enemyTypeNumber == 1) { //determine the points added to score and displayed on screen after each kill
          scoreToBeDisplayed = (int)((Math.random() * 31) + 40); 
        } else if (enemyTypeNumber == 2) {
          scoreToBeDisplayed = (int)((Math.random() * 31) + 100); 
        } else if (enemyTypeNumber == 3) {
          scoreToBeDisplayed = (int)((Math.random() * 31) + 70); 
        } else if (enemyTypeNumber == 4) {
          scoreToBeDisplayed = (int)((Math.random() * 31) + 140); 
        } else if (enemyTypeNumber == 5) {
          scoreToBeDisplayed = (int)((Math.random() * 31) + 90); 
        } else if (enemyTypeNumber == 6) {
          scoreToBeDisplayed = (int)((Math.random() * 31) + 110); 
        }
        currentScore += scoreToBeDisplayed; //add score to TOTAL
        
        if ((int)(Math.random() * 15) == 1) { // 1/15 chance to spawn 
          powerups.add(new Drop("EXTRA_SCORE", (x + (enemyImage.getWidth() / 2)), y));
        }
        
        new Sound("Sounds/explosionSound.wav");
        remove(); 
        explode(scoreToBeDisplayed, scale);

      }
    }

    public void explode(int scoreToBeDisplayed, int scale) {
      //establish animation with frame 1            
      animationFrame = new JLabel(new ImageIcon("enemyExplosion_animation/explosion frame " + animationFrameValue + ".png"));
      animationFrame.setBounds(new Rectangle(new Point((int)(getCenter().getX() - (animationFrame.getPreferredSize().getWidth() / 2)),
          (int)(getCenter().getY() - (animationFrame.getPreferredSize().getHeight() / 2))), animationFrame.getPreferredSize()));


      new Timer().scheduleAtFixedRate(new TimerTask() {
        public void run() {                 
          //use an image from the image icon to scale it before adding it into the JLabel. This is done for each frame
          ImageIcon explosionIcon = new ImageIcon("enemyExplosion_animation/explosion frame " + animationFrameValue + ".png"); 
          Image scaled = explosionIcon.getImage().getScaledInstance(explosionIcon.getIconWidth() + scale,
              explosionIcon.getIconHeight() + scale, java.awt.Image.SCALE_SMOOTH);

          if (animationFrameValue > 62) {
            this.cancel(); //end animation timer
            GameScreen.remove(animationFrame);
            animationFrame.setVisible(false); //remove JLabel for the animation
            try {
              displayScore(scoreToBeDisplayed , killByFire); //will call this method always when killed but only display score on screen if enemy shot to death
            }
            catch(IllegalArgumentException e) { //if the explosion exists in an illegal state with the points indicator, catch the exception
            }
          }

          if (!isPaused) { //when the game is paused, the animation is also frozen
            animationFrame.setIcon(new ImageIcon(scaled));
            if (animationFrameValue == 1) { //the actual JLabel is added only once, specifically via the first frame as shown here
              try {
                GameScreen.add(animationFrame);
                GameScreen.moveToFront(animationFrame);
              }
              catch(IllegalArgumentException e) {
              }
            }
            animationFrameValue++; //use setImage on the JLabel and loop until 62 then delete JLabel
          }
        }
      }, 17, 17);

    }


    public void displayScore(int scoreGainedDisplay, boolean killByFire) { //indicates if the enemy was shot or collided with

      //shows the points earned per kill on screen
      if (killByFire) {
        JLabel pointsIndication = new JLabel("+" + scoreGainedDisplay); 
        pointsIndication.setFont(new Font("Times Roman", Font.PLAIN, 20)); 
        pointsIndication.setForeground(new Color(255, 190, 0));
        pointsIndication.setBounds(new Rectangle(new Point((int)(getCenter().getX() - (pointsIndication.getPreferredSize().getWidth() / 2)), //center of enemy image
            (int)(getCenter().getY() - (pointsIndication.getPreferredSize().getHeight() / 2))), pointsIndication.getPreferredSize()));    
        try {
          GameScreen.add(pointsIndication);
          GameScreen.moveToFront(pointsIndication);
        }
        catch(IllegalComponentStateException e){ //avoid glitches if the explosion is in an illegal position on the screen
        }

        new Timer().schedule(new TimerTask() {
          public void run() {
            pointsIndication.setVisible(false);
            GameScreen.remove(pointsIndication);
          }
        }, 800);
      }
    }
  }



  public static class Drop {

    private JLabel dropImage;
    private int x, y, rate;
    private Timer myTimer; //for falling
    public Timer countdown; //count down and tick for depleting the timer of lingering effects
    private TimerTask tick; 

    public Drop(String type, int xPos, int yPos) {
      this.x = xPos;
      this.y = yPos;
      this.rate = 2; //by default the period rate for the falling timer is 2 milliseconds

      //image is determined by the type of drop spawned
      if(type.equals("GAIN_ENERGY"))   //this drop will give the player ship one fuel unit. Necessary to progress
        dropImage = new JLabel(new ImageIcon("Images/fuelDrop.png"));                       
      else if(type.equals("COOLDOWN")) 
        dropImage = new JLabel(new ImageIcon("Images/cooldownDrop.png"));         
      else if(type.equals("HEALTH")) 
        dropImage = new JLabel(new ImageIcon("Images/healthDrop.png"));       
      else if(type.equals("RAPID_FIRE")) 
        dropImage = new JLabel(new ImageIcon("Images/rapidfireDrop.png"));    
      else if(type.equals("INVINCIBILITY")) 
        dropImage = new JLabel(new ImageIcon("Images/invincibilityDrop.png"));   
      else if(type.equals("DISABLE_ENEMY")) 
        dropImage = new JLabel(new ImageIcon("Images/disableDrop.png"));

      else if(type.equals("ROCKETS")) //weapon supply images
        dropImage = new JLabel(new ImageIcon("Images/rocketWeapon.png"));
      else if(type.equals("LASER")) 
        dropImage = new JLabel(new ImageIcon("Images/laserWeapon.png"));  
      else if(type.equals("SMART_ROCKETS")) 
        dropImage = new JLabel(new ImageIcon("Images/smartRocketWeapon.png"));  
      else if(type.equals("MACHINE_BLAST")) 
        dropImage = new JLabel(new ImageIcon("Images/machineBlastWeapon.png"));  
      else if(type.equals("SPREAD_BLAST")) 
        dropImage = new JLabel(new ImageIcon("Images/spreadBlastWeapon.png"));
      else if(type.equals("EXTRA_SCORE")) {
        dropImage = new JLabel(new ImageIcon("Images/extraScoreDrop.png"));
        this.x -= dropImage.getIcon().getIconWidth() / 2; //spawn in the middle of where the enemy ship was destroyed
        this.rate = 4; //star bonus falls slower
      } 
      
      dropImage.setLayout(null);
      dropImage.setBounds(new Rectangle(new Point(x, y), dropImage.getPreferredSize()));
      try {
        GameScreen.add(dropImage);
        GameScreen.moveToFront(dropImage);
      }
      catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e) {                
      }            

      TimerTask falling = new TimerTask() {
        public void run() {
          if (y >= GameScreen.getHeight() + 50) {
            remove(); //when past the screen, remove the drop
          } else {
            moveDrop();
            if (getHitbox().intersects(ship.getShipHitbox())) { //if the ship collects a drop
              remove(); //if the drop hits the user ship, remove it and generate effect based on drop type
              switch (type) { //switch case more fitting since there will be multiple power ups in the game
              case "GAIN_ENERGY":
                currentEnergyState += 20; //add a fifth of the full energy bar
                if (currentEnergyState > 100) {
                  currentEnergyState = 100; //cap at 100 units of energy
                }
                fuelBar.updateFuelBar();
                break;
              case "COOLDOWN":
                currentHeatState = 0; //instant cool down to 0 heat units
                heatBar.updateHeatBar();
                break;
              case "HEALTH":
                ship.damage(-200); //increases ship health value by 200. bar updated within damage method
                break;
              case "RAPID_FIRE": 
                terminate(); //shuts of all lingering effects, resets the duration time, and cancels the count down timers on all spawned drops
                
                rapid = true; 
                timeLeft = 15;
                
                tick = new TimerTask() {
                  public void run() {
                    if (rapid) {
                      durationBarLengthRatio = (double)timeLeft / 15;
                      updateDropCountdown("Images/rapidfireDrop.png");
                    }
                    if(timeLeft <= 0 || !rapid) {
                      terminate(); //drop effect duration = 0, effects become all false                   
                      updateDropCountdown("Images/blankDrop.png"); //sets timer to 0 and the icon back to a blank power up
                    } 
                    if(!isPaused)
                      timeLeft--; //for 15 seconds, rapid fire is active   
                  }
                };
                countdown = new Timer("myTimer");
                countdown.scheduleAtFixedRate(tick, 0, 1000);
                break;
              case "INVINCIBILITY":   
                terminate();

                invincible = true;
                timeLeft = 10;
                
                tick = new TimerTask() {
                  public void run() {
                    if (invincible) {
                      durationBarLengthRatio = (double)timeLeft / 10;
                      updateDropCountdown("Images/invincibilityDrop.png");
                    }
                    if(timeLeft <= 0 || !invincible) {
                      terminate();                  
                      updateDropCountdown("Images/blankDrop.png"); //sets timer to 0 and the icon back to a blank power up
                    }
                    if(!isPaused)
                      timeLeft--; //for 10 seconds, damage immunity is active   
                  }
                };
                countdown = new Timer("myTimer");
                countdown.scheduleAtFixedRate(tick, 0, 1000);   
                break;
              case "DISABLE_ENEMY":
                terminate();

                enemyFireDisabled = true;
                timeLeft = 15;

                try { //non laser bullets are unchanged as they have already been shot and cannot be "disabled" 
                  for(Enemy nextEnemy: enemies) { 
                    for(enemyProjectile projectile: nextEnemy.lasers) {
                      projectile.remove(true); //likewise, lasers however, are shut off when this effect activates
                    }
                  }
                }
                catch(NullPointerException e) {
                }

                tick = new TimerTask() {
                  public void run() {
                    if (enemyFireDisabled) {
                      durationBarLengthRatio = (double)timeLeft / 15;
                      updateDropCountdown("Images/disableDrop.png");
                    }
                    if(timeLeft <= 0 || !enemyFireDisabled) {
                      terminate();                  
                      updateDropCountdown("Images/blankDrop.png"); //sets timer to 0 and the icon back to a blank power up
                    }
                    if(!isPaused)
                      timeLeft--; //for 15 seconds, damage enemy fire disability is active   
                  }
                };
                countdown = new Timer("myTimer");
                countdown.scheduleAtFixedRate(tick, 0, 1000);
                break;
              case "ROCKETS":
                rocketCount += 4; //+4 pairs of rockets
                rockets.setText("[" + rocketCount + "]");
                break;
              case "LASER":
                laserCount += 2; //+2 lasers
                lasers.setText("[" + laserCount + "]");
                break;
              case "SMART_ROCKETS":
                smartRocketCount += 3; //+3 pairs of smart rockets
                smartRockets.setText("[" + smartRocketCount + "]");
                break;
              case "MACHINE_BLAST":
                machineBlasterCount += 6; //+8 machine blaster bursts
                machineBlaster.setText("[" + machineBlasterCount + "]");
                break;
              case "SPREAD_BLAST":
                spreadBlastCount += 4; //+4 spread blast shots
                spreadBlast.setText("[" + spreadBlastCount + "]");
                break;
              case "EXTRA_SCORE": //bonus "drop," not a power up. chance to spawn when enemy is killed and gives point boost
                currentScore += 500;
                break;
              }

            }
          }
        }                   
      };
      myTimer = new Timer("myTimer");
      myTimer.scheduleAtFixedRate(falling, 0, rate);                        
    }

    public void moveDrop() {
      if(!isPaused) {
        this.y += 1;                
        dropImage.setLayout(null);
        dropImage.setBounds(new Rectangle(new Point(x, y), dropImage.getPreferredSize()));
      }
    }

    public void remove() { //in this case, remove is meant to remove the physical drop from the screen when it goes off screen or is collected
      dropImage.setVisible(false);
      GameScreen.remove(dropImage); //removes from screen
      myTimer.cancel();
    }

    public Rectangle getHitbox() {
      return dropImage.getBounds();
    }

    public void terminate() { //terminates all timed/lingering effects to reset effect status
      invincible = false; 
      rapid = false;
      enemyFireDisabled = false;
      timeLeft = 0; //resets time back to zero. this also naturally ends the power ups effect as well
      durationBarLengthRatio = 0.0;
      for (Drop drop: powerups) { 
        if (drop != null && drop.countdown != null) { //prevents canceling a null timer
          drop.countdown.cancel();
        }
      }
    }

  }



  public static class enemyProjectile {

    private int x, y; 
    private double type;
    private double speed;
    private JLabel shotBullet;
    private Timer myTimer;
    private JLabel flareEffect; //for lasers and other similar weapons

    public enemyProjectile(int xPos, int yPos, int damage, double thisEnemyBulletType, int changeX, int changeY, int curve) {
      this.type = thisEnemyBulletType;

      if(type != 4 && type != 5) { //establish y and speed
        this.y = yPos + changeY; //directly in front of the ship
        this.speed = 1;
      }
      else if(type == 4){
        this.y = yPos + changeY - 45; //at tip of blaster of enemy 4
        this.speed = 0; //nearly unrecognizable slow speed for the purpose of collision checking
      }
      else if(type == 5) {
        this.y = yPos + changeY; //almost at very front of enemy 5
        this.speed = 0; 
      }

      //for visual appearance, x values are adjusted since the images are corner oriented and differ between each enemy ship type
      if(type == 1) { 
        shotBullet = new JLabel(new ImageIcon("Images/enemy1Bullet.png"));
        this.x = (xPos + (changeX / 2)) - (shotBullet.getIcon().getIconWidth() / 2); //in the center
        new Sound("Sounds/enemy1Sound.wav"); //play the sounds of the different enemy blasts
      }           
      else if(type == 2) {
        shotBullet = new JLabel(new ImageIcon("Images/enemy2Bullet.png"));
        if(changeX > 0) { //if the x adjustment is not 0 this suggests that the image should be shifted
          this.x = (xPos + changeX) - shotBullet.getIcon().getIconWidth(); //on one of either side of the image
        }
        else {
          this.x = xPos; //double shot
        }
        new Sound("Sounds/enemy2Sound.wav");
      }
      else if(type == 3) {
        shotBullet = new JLabel(new ImageIcon("Images/enemy3Bullet.png"));
        this.x = (xPos + (changeX / 2)) - (shotBullet.getIcon().getIconWidth() / 2); //in the center
        new Sound("Sounds/enemy3Sound.wav");
      }           
      else if(type == 4) {
        shotBullet = new JLabel(new ImageIcon("Images/enemy4Bullet.png"));
        this.x = (xPos + (changeX / 2)) - (shotBullet.getIcon().getIconWidth() / 2); //in the center

        flareEffect = new JLabel(new ImageIcon("Images/enemy4BulletFlare.png"));
        flareEffect.setLayout(null);
        flareEffect.setBounds(new Rectangle(new Point(xPos - (int)((flareEffect.getPreferredSize().getWidth() - changeX) / 2), y - (changeY / 3)), flareEffect.getPreferredSize()));
        new Sound("Sounds/enemy4Sound.wav");
      }
      else if(type == 5) {
        shotBullet = new JLabel(new ImageIcon("Images/enemy5Bullet.png"));              
        if(changeX > 0) { 
          this.x = xPos + (changeX - 40 - (shotBullet.getIcon().getIconWidth() / 2)); 
        }
        else {
          this.x = xPos + (40 - (shotBullet.getIcon().getIconWidth() / 2)); //double laser
        }

        flareEffect = new JLabel(new ImageIcon("Images/enemy5BulletFlare.png"));
        flareEffect.setLayout(null);
        flareEffect.setBounds(new Rectangle(new Point(x - 24, y - (int)(flareEffect.getPreferredSize().getHeight() / 2)), flareEffect.getPreferredSize()));
        new Sound("Sounds/enemy5Sound.wav");
      }
      else if(type == 5.2) {
        shotBullet = new JLabel(new ImageIcon("Images/enemy5Bullet2.png"));
        this.x = (xPos + (changeX / 2)) - (shotBullet.getIcon().getIconWidth() / 2);
        new Sound("Sounds/enemy5Sound2.wav");
      }
      else if(type == 6) {
        shotBullet = new JLabel(new ImageIcon("Images/enemy6Bullet.png"));
        this.x = (xPos + (changeX / 2)) - (shotBullet.getIcon().getIconWidth() / 2);
        new Sound("Sounds/enemy6Sound.wav");
      }


      shotBullet.setLayout(null);
      shotBullet.setBounds(new Rectangle(new Point(x, y), shotBullet.getPreferredSize()));
      try { //avoid creating image at illegal bounds
        if(type == 4 || type == 5) {
          GameScreen.add(flareEffect);
          GameScreen.moveToFront(flareEffect);
        }
        GameScreen.add(shotBullet);
        GameScreen.moveToFront(shotBullet);
      }
      catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e) {                
      }

      if(type != 6) {
        TimerTask moveProjectile = new TimerTask() {
          public void run() {
            if(y > GameScreen.getHeight() + 100 || y < -100) { //delete when off screen
              remove(true);                        
            }
            else {

              updateBulletY((int)speed); //move enemy downwards like a typical bullet

              if(getHitbox().intersects(ship.getShipHitbox())) {
                ship.damage(damage); //regardless of bullet type, the damaging ability is removed upon first impact to avoid instant defeat
                if(type == 4 || type == 5) { //lasers in this case, will deal damage once, but will linger on screen until the enemy ceases firing or is defeated
                  remove(false); //if a the bullet image should linger such as with lasers, do not remove it upon collision immediately
                }
                else {
                  remove(true); //remove the image it upon impact if not a laser
                }
              }
            }
          }     
        };     
        myTimer = new Timer("myTimer");
        myTimer.scheduleAtFixedRate(moveProjectile, 0, 1);
      }
      else if(type == 6) {
        TimerTask moveProjectile = new TimerTask() {
          public void run() {

            updateBulletY(7 * (int)speed); //relative to the speed downwards, the spread will be more condensed
            updateBulletX(curve); //curve parameter used to determine diagonal movement speed by spread out projectiles

            if(getHitbox().intersects(ship.getShipHitbox())) { //when the projectile collides
              ship.damage(damage);
              remove(true); //remove the image it upon impact
            }

          }     
        };     
        myTimer = new Timer("myTimer");
        myTimer.scheduleAtFixedRate(moveProjectile, 0, 20); //DEFAULT 20 for period


      }
    }

    public void updateBulletX(int magnitude) {
      if(!isPaused) {
        this.x += magnitude;
        shotBullet.setLayout(null);
        shotBullet.setBounds(new Rectangle(new Point(x, y), shotBullet.getPreferredSize()));
      }
    }

    public void updateBulletY(int magnitude) {
      if(!isPaused) {
        this.y += magnitude;
        shotBullet.setLayout(null);
        shotBullet.setBounds(new Rectangle(new Point(x, y), shotBullet.getPreferredSize()));
      }
    }

    public void remove(boolean removeImage) { //remove image and movement timer
      if(removeImage) { //if the bullet image is to be removed from screen or not
        shotBullet.setVisible(false);
        GameScreen.remove(shotBullet);
        if(type == 4 || type == 5) { //removes visual flare from ships bullets if present
          flareEffect.setVisible(false);
          GameScreen.remove(flareEffect);
        }
      }
      myTimer.cancel();
    }

    public Rectangle getHitbox() {
      return shotBullet.getBounds();
    }

  }



  public static class Formation {

    private int enemyType;
    private int count = 0;
    private int divider;

    public Formation(int formationType) {

      switch(formationType) {
      case 1: //side by side enemies 1 or 3
        divider = 50;
        if(new Random().nextBoolean())
          enemyType = 1;
        else 
          enemyType = 3;
        Enemy enemy1 = new Enemy((int)(Math.random() * (GameScreen.getWidth() - divider)), -100, enemyType);
        enemies.add(enemy1);
        enemies.add(new Enemy((int)enemy1.getHitbox().getX() + divider, -100, enemyType));
        break;
      case 2: //line of enemy 1
        int randX = (int)(Math.random() * (GameScreen.getWidth() - 50));
        new Timer().scheduleAtFixedRate(new TimerTask() {
          public void run() {
            if(count == 4) 
              this.cancel();
            else 
              enemies.add(new Enemy(randX, -100, 1));
            count++;
          }
        }, 0, 200);
        break;
      case 3: //three enemy 4 ships 
        Enemy first = new Enemy(100, -100, 4);
        enemies.add(first);
        enemies.add(new Enemy((int)((GameScreen.getWidth() / 2) - (first.getHitbox().getWidth() / 2)) , -100, 4));
        enemies.add(new Enemy(GameScreen.getWidth() - (int)(first.getHitbox().getWidth() + 100), -100, 4));
        break;
      case 4: //enemy 1 v formation
        divider = 0;
        Enemy front = new Enemy((int)(Math.random() * (GameScreen.getWidth() - 300) + 150), -100, 1); //add some room to fit the formation
        enemies.add(front);
        new Timer().scheduleAtFixedRate(new TimerTask() {
          public void run() {
            divider++; //since the object is oriented from the leftmost corner, the side by side formation requires different +/- in distance between ships between the left vs right side of the formation
            enemies.add(new Enemy((int)(front.getCenter().getX() - (front.getHitbox().getWidth() / 2) - (divider * front.getHitbox().getWidth())), -100, 1));
            divider--;
            enemies.add(new Enemy((int)(front.getCenter().getX() + (front.getHitbox().getWidth() / 2) + (divider * front.getHitbox().getWidth())), -100, 1));
            if(divider == 2) //two on each side of the middle ship
              this.cancel();
            divider++;
          }
        }, 150, 150);
        break;
      case 5: //enemy 2 triangle
        Enemy leftEnemy = new Enemy(((int)(Math.random() * GameScreen.getWidth() - 100)) + 50, -100, 2);
        enemies.add(leftEnemy);
        Enemy centerEnemy = new Enemy((int)leftEnemy.getCenter().getX(), -170, 2);
        enemies.add(centerEnemy);
        Enemy rightEnemy = new Enemy((int)centerEnemy.getCenter().getX(), -100, 2);
        enemies.add(rightEnemy);
        break;
      }

    }

  }



  public static class Sound { //initializes and plays sound

    private Clip soundClip; 
    private FloatControl volumeEdit;

    public Sound(String path) {

      try {
        AudioInputStream Stream = AudioSystem.getAudioInputStream(new File(path)); //create stream to read in audio file            
        soundClip = AudioSystem.getClip(); //obtain and initialize a clip to play sound from the created audio stream
        soundClip.open(Stream); //designate this clip to this sound import stream
      } 
      catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) { //catch file and audio stream exceptions
      } 

      volumeEdit = (FloatControl) soundClip.getControl(FloatControl.Type.MASTER_GAIN); //volume edit controls the sound clips volume gain when played
      volumeEdit.setValue(-0.5f);

      if(path.equals("Sounds/secondaryBulletSound.wav")) {
        volumeEdit.setValue(-15);
      }
      else if(path.equals("Sounds/mouseOverSound.wav") || path.equals("Sounds/enemy1Sound.wav") 
          || path.equals("Sounds/enemy2Sound.wav") || path.equals("Sounds/enemy3Sound.wav")  || path.equals("menuMusic.wav")) {             
        volumeEdit.setValue(-30); 
      }
      else if(path.equals("Sounds/enemy5Sound2.wav") || path.equals("Sounds/missileWeaponSound.wav") || path.equals("Sounds/explosionSound.wav")) {
        volumeEdit.setValue(-30); 
      }

      if(path.equals("Sounds/menuMusic.wav") || path.equals("Sounds/gameMusic1.wav") || path.equals("Sounds/gameMusic2.wav")
          || path.equals("Sounds/gameMusic3.wav") || path.equals("Sounds/gameMusic4.wav")) {
        volumeEdit.setValue(-5); //reduce volume for sounds only, not music
      }
      else {
        volumeEdit.setValue(-10);
      }

      if(!soundOn) {//if sound is enabled
        soundClip.start(); //play music or sound object
      }
    }

    public Clip getClip() { //gets access to the sound objects audio clip
      return soundClip;
    }

  }
}




