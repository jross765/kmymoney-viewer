package org.kmymoney.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.kmymoney.viewer.actions.AccountAction;
import org.kmymoney.viewer.actions.OpenAccountInNewTab;
import org.kmymoney.viewer.actions.OpenAccountInNewWindow;
import org.kmymoney.viewer.actions.TransactionSplitAction;
import org.kmymoney.viewer.models.KMyMoneyAccountsTreeModel;
import org.kmymoney.viewer.models.KMyMoneyFileImpl;
import org.kmymoney.viewer.panels.TransactionsPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

/**
 * Simple Viewer for KMyMoney files.
 */
@SuppressWarnings("serial")
public class JKMyMoneyViewer extends JFrame {
	
	enum StartMode {
		REGULAR,
		OPEN_ACCOUNT,
		OPEN_ACCOUNT_TRANSACTION,
		OPEN_ACCOUNT_TRANSACTION_SPLIT
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(JKMyMoneyViewer.class);

	// ---------------------------------------------------------------

	private static final String TITLE = "JKMyMoney Viewer";

	private static final int DEFAULT_WIDTH  = 750;
	private static final int DEFAULT_HEIGHT = 600;
	
	private static final double DIVIDER_RATIO = 0.3;

	// ---------------------------------------------------------------
	// Command line args
	
	// private static PropertiesConfiguration cfg = null;
	private static Options options;
	  
	private static String          kmmFileName  = null;
	private static StartMode       startMode    = null;
	private static KMMAcctID       acctID       = null; // sic, not KMMCompxAcctID
	private static KMMTrxID        trxID        = null;
	private static KMMSpltID       spltID       = null;
	private static KMMQualifSpltID qualifSpltID = null;

	// ---------------------------------------------------------------

	/**
	 * Wrapper for an {@link AccountAction} that knows about {@link JKMyMoneyViewer#getSelectedAccount()}.
	 */
	private final class AccountActionWrapper implements Action {
		/**
		 * The {@link AccountAction} we are wrapping.
		 */
		private final AccountAction myAccountAction;

		/**
		 */
		private AccountActionWrapper(final AccountAction anAccountAction) {
			myAccountAction = anAccountAction;
		}

		@Override
		public void addPropertyChangeListener(final PropertyChangeListener aListener) {
			myAccountAction.addPropertyChangeListener(aListener);
		}

		@Override
		public Object getValue(final String aKey) {
			return myAccountAction.getValue(aKey);
		}

		@Override
		public boolean isEnabled() {
			try {
				myAccountAction.setAccount(getSelectedAccount());
				return myAccountAction.isEnabled();
			} catch (Exception e) {
				LOGGER.error("isEnabled: Cannot query isEnabled for AccountAction", e);
				return false;
			}
		}

		@Override
		public void putValue(final String aKey, final Object aValue) {
			myAccountAction.putValue(aKey, aValue);
		}

		@Override
		public void removePropertyChangeListener(final PropertyChangeListener aListener) {
			myAccountAction.removePropertyChangeListener(aListener);
		}

		@Override
		public void setEnabled(final boolean aB) {
			myAccountAction.setEnabled(aB);
		}

		@Override
		public void actionPerformed(final ActionEvent aE) {
			try {
				myAccountAction.setAccount(getSelectedAccount());
				myAccountAction.actionPerformed(aE);
			} catch (Exception e) {
				LOGGER.error("actionPerformed: Cannot execute AccountAction", e);
			}
		}

		/**
		 * @return the accountAction we are wrapping.
		 */
		public AccountAction getAccountAction() {
			return myAccountAction;
		}
	}
	
	// ---------------------------------------------------------------

	private KMyMoneyFileImpl myModel;

	private javax.swing.JPanel jContentPane = null;

	private javax.swing.JScrollPane treeScrollPane = null;

	private javax.swing.JFileChooser jFileChooser = null;

	/**
	 * The currently selected account.
	 */
	private KMyMoneyAccount selectedAccount = null;

	/**
	 * The split-pane between account-tree and transactions-table.
	 */
	protected JSplitPane jSplitPane = null;

	/**
	 * The tree showing all accounts.
	 */
	private JTree accountsTree = null;
	/**
	 * The {@link JTabbedPane} containing {@link #transactionsPanel}
	 * and {@link #taxReportPanel}.
	 */
	private JTabbedPane myTabbedPane = null;
	private TransactionsPanel transactionsPanel = null;
	private JMenuBar jJMenuBar = null;
	/**
	 * The File-Menu.
	 */
	private JMenu myFileMenu = null;

	/**
	 * File->Load.
	 */
	private JMenuItem myFileLoadMenuItem = null;
	/**
	 * File->Exit.
	 */
	private JMenuItem myFileExitMenuItem = null;

	/**
	 * Popup-menu on the account-tree.
	 */
	private JPopupMenu myAccountTreePopupMenu;

	/**
	 * The actions we have on accounts.
	 */
	private Collection<AccountAction> myAccountActions;

	/**
	 * The actions we have on Splits.
	 */
	private Collection<TransactionSplitAction> mySplitActions;

	// ---------------------------------------------------------------

	/**
	 * This method initializes
	 * the KMyMoneyViewer.
	 */
	public JKMyMoneyViewer() {
		super();
	}

	// ---------------------------------------------------------------

	public static void main( final String[] args ) {
		try {
			JKMyMoneyViewer viewer = new JKMyMoneyViewer();
			viewer.execute( args );
		} catch ( CouldNotExecuteException exc ) {
			System.err.println( "Execution exception. Aborting." );
			exc.printStackTrace();
			System.exit( 1 );
		}
	}

	public void execute(String[] args) throws CouldNotExecuteException {
		// Initialize
		try {
			init();
		} catch ( Exception exc ) {
			System.err.println( "Could not initialize environment." );
			exc.printStackTrace();
			throw new CouldNotExecuteException();
		}

		// Parse command line
		try	{
			parseCommandLineArgs( args );
		} catch ( Exception exc ) {
			System.err.println( "Invalid command line args." );
			printUsage();
			throw new CouldNotExecuteException();
		}

		try	{
			kernel();
		} catch ( Exception exc ) {
			System.err.println( "Error in Tool kernel." );
			exc.printStackTrace();
			throw new CouldNotExecuteException();
		}
	}

	protected void kernel() throws Exception {
		// KMyMoneyFileImpl kmmFile = new KMyMoneyFileImpl( new File( kmmFileName ) );

		installNimbusLaF();
		initializeGUI();
		setVisible(true);
		loadFile(new File(kmmFileName));
		getJSplitPane().setDividerLocation(DIVIDER_RATIO);
		
		if ( startMode == StartMode.OPEN_ACCOUNT ) {
			KMyMoneyAccount acct = myModel.getAccountByID( acctID );
			LOGGER.debug( "kernel: Found account: " + acct);
			LOGGER.debug( "kernel: Opening new account window with account " + acctID);
			// https://learn-it-university.com/manually-invoking-actions-in-swing-a-step-by-step-guide/
			Action customAction = new OpenAccountInNewWindow(acct);
			ActionEvent manualEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "manualInvocation");
			customAction.actionPerformed(manualEvent);
		} else if ( startMode == StartMode.OPEN_ACCOUNT_TRANSACTION ) {
			KMyMoneyTransaction trx = myModel.getTransactionByID( trxID );
			LOGGER.debug( "kernel: Found transaction: " + trx);
			KMyMoneyTransactionSplit splt = myModel.getTransactionSplitByAcctIDAndTrxID( acctID, trxID );
			LOGGER.debug( "kernel: Found transaction split: " + splt);
			LOGGER.debug( "kernel: Opening new account window with account " + acctID + " and transaction " + trxID + " and split " + splt.getID());
			// https://learn-it-university.com/manually-invoking-actions-in-swing-a-step-by-step-guide/
			Action customAction = new OpenAccountInNewWindow(splt, false);
			ActionEvent manualEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "manualInvocation");
			customAction.actionPerformed(manualEvent);
		} else if ( startMode == StartMode.OPEN_ACCOUNT_TRANSACTION_SPLIT ) {
			KMyMoneyTransactionSplit splt = myModel.getTransactionSplitByID( qualifSpltID );
			LOGGER.debug( "kernel: Found transaction split: " + splt);
			LOGGER.debug( "kernel: Opening new account window with acount " + splt.getAccountID() + " and transaction " + splt.getTransactionID() + " and split " + spltID);
			// https://learn-it-university.com/manually-invoking-actions-in-swing-a-step-by-step-guide/
			Action customAction = new OpenAccountInNewWindow(splt, true);
			ActionEvent manualEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "manualInvocation");
			customAction.actionPerformed(manualEvent);
		}
	}
	
	// ---------------------------------------------------------------
	
	protected void init() throws Exception {
		acctID       = new KMMAcctID();
		trxID        = new KMMTrxID();
		spltID       = new KMMSpltID();
		// qualifSpltID = new KMMQualifSpltID();

//	    cfg = new PropertiesConfiguration(System.getProperty("config"));
//	    getConfigSettings(cfg);

		// Options
		// The essential ones
		Option optFile = Option.builder( "f" )
				.required()
				.hasArg()
				.argName( "file" )
				.desc( "KMyMoney file" )
				.longOpt( "kmymoney-file" )
				.get();

		// The convenient ones
	    Option optAcctID = Option.builder("acct")
	    		.hasArg()
	    		.argName("acctid")
	    		.desc("Account-ID")
	    		.longOpt("account-id")
	    		.get();
		
	    Option optTrxID = Option.builder("trx")
	    		.hasArg()
	    		.argName("trxid")
	    		.desc("Transaction ID")
	    		.longOpt("transaction-id")
	    		.get();
		
	    Option optSpltID = Option.builder("splt")
	    		.hasArg()
	    		.argName("spltid")
	    		.desc("Transaction split-ID " +
	    			  "(non-qualified, use together with <transaction-id>)")
	    		.longOpt("split-id")
	    		.get();
		

		options = new Options();
		options.addOption( optFile );
		options.addOption( optAcctID );
		options.addOption( optTrxID );
		options.addOption( optSpltID );
	}

	protected void getConfigSettings(PropertiesConfiguration cs) throws Exception {
		// ::EMPTY
	}

	protected void parseCommandLineArgs(String[] args) throws InvalidCommandLineArgsException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmdLine = null;
		try {
			cmdLine = parser.parse( options, args );
		} catch ( ParseException exc ) {
			System.err.println( "Parsing options failed. Reason: " + exc.getMessage() );
			throw new InvalidCommandLineArgsException();
		}

		// ---

		// <kmymoney-file>
		try {
			kmmFileName = cmdLine.getOptionValue( "kmymoney-file" );
		} catch ( Exception exc ) {
			System.err.println( "Could not parse <kmymoney-file>" );
			throw new InvalidCommandLineArgsException();
		}

		System.err.println( "KMyMoney file:     '" + kmmFileName + "'" );
		
		// --
		
		startMode = StartMode.REGULAR; // not final, it's just the start point 
		
		// --

	    // <account-id>
	    if ( cmdLine.hasOption("account-id") ) {
	    	if ( startMode != StartMode.REGULAR ) {
	    		System.err.println("<account-id> cannot be set because another ID has already been set");
	    		throw new InvalidCommandLineArgsException();
	    	}
//	      if ( mode != Helper.Mode.ID ) {
//	        System.err.println("<account-id> must only be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
//	        throw new InvalidCommandLineArgsException();
//	      }
	      
	      try {
	        acctID = new KMMAcctID( cmdLine.getOptionValue("account-id") );
	        startMode = StartMode.OPEN_ACCOUNT;
	      } catch ( Exception exc ) {
	        System.err.println("Could not parse <account-id>");
	        throw new InvalidCommandLineArgsException();
	      }
	    } else {
//	      if ( mode == Helper.Mode.ID ) {
//	        System.err.println("<account-id> must be set with <mode> = '" + Helper.Mode.ID.toString() + "'");
//	        throw new InvalidCommandLineArgsException();
//	      }
	    	int dummy = 0;
	    }
	    
	    System.err.println("Account ID:         " + acctID);
	    
	    // <transaction-id>
	    if ( cmdLine.hasOption("transaction-id") ) {
	    	if ( startMode != StartMode.REGULAR &&
	    		 startMode != StartMode.OPEN_ACCOUNT ) {
	    		System.err.println("<transaction-id> cannot be set because another ID has already been set");
	    		throw new InvalidCommandLineArgsException();
	    	}
	      
	    	try {
	    		trxID = new KMMTrxID(  cmdLine.getOptionValue("transaction-id") );
	    		startMode = StartMode.OPEN_ACCOUNT_TRANSACTION; // for the moment -- could change to OPEN_TRANSACTION_SPLIT 
	    	} catch ( Exception exc ) {
	    		System.err.println("Could not parse <transaction-id>");
	    		throw new InvalidCommandLineArgsException();
	    	}
	    }
	    
	    System.err.println("Transaction ID:     " + trxID);
	    
	    // <split-id>
	    if ( cmdLine.hasOption("split-id") ) {
	    	if ( startMode != StartMode.REGULAR &&
	    		 startMode != StartMode.OPEN_ACCOUNT_TRANSACTION ) { // <-- notice
	    		System.err.println("<split-id> cannot be set because another ID has already been set");
	    		throw new InvalidCommandLineArgsException();
	    	}
	      
	    	try {
	    		spltID = new KMMSpltID( cmdLine.getOptionValue("split-id") );
	    		qualifSpltID = new KMMQualifSpltID( trxID, spltID );
	    		startMode = StartMode.OPEN_ACCOUNT_TRANSACTION_SPLIT;
	    	} catch ( Exception exc ) {
	    		System.err.println("Could not parse <split-id>");
	    		throw new InvalidCommandLineArgsException();
	    	}
	    }
	    
	    System.err.println("Split ID:           " + spltID);
	    System.err.println("Qualif. split ID:   " + qualifSpltID);
	    
	    // ---
	    
	    System.err.println("Start mode:         " + startMode);
	}

	protected void printUsage()
	{
		HelpFormatter formatter = HelpFormatter.builder().get();
		try {
			formatter.printHelp( "JKMyMoneyViewer", "", options, "", true );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ---------------------------------------------------------------

	protected static void installNimbusLaF() {
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * This method initializes jSplitPane.
	 *
	 * @return javax.swing.JSplitPane
	 */
	protected JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setLeftComponent(getTreeScrollPane());
			jSplitPane.setRightComponent(getJTabbedPane());
		}
		
		return jSplitPane;
	}

	/**
	 * This method initializes accountsTree.
	 *
	 * @return javax.swing.JTree
	 */
	protected JTree getAccountsTree() {
		if (accountsTree == null) {
			accountsTree = new JTree();

			if (getModel() == null) {
				accountsTree.setModel(new DefaultTreeModel(null));
			} else {
				accountsTree.setModel(new KMyMoneyAccountsTreeModel(getModel()));
			}
			
			accountsTree.addMouseListener(new MouseAdapter() {

				/** show popup if mouseReleased is a popupTrigger on this platform.
				 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseReleased(final MouseEvent aE) {
					if (aE.isPopupTrigger()) {
						getAccountTreePopupMenu().show((JComponent) aE.getSource(), aE.getX(), aE.getY());
					}
				}

				/** show pop-up if mousePressed is a popupTrigger on this platform.
				 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
				 */
				@Override
				public void mousePressed(final MouseEvent aE) {
					if (aE.isPopupTrigger()) {
						getAccountTreePopupMenu().show((JComponent) aE.getSource(), aE.getX(), aE.getY());
					}
				}
			});

			accountsTree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(final TreeSelectionEvent e) {

					TreePath path = e.getPath();
					if (path == null) {
						setSelectedAccount(null);
					} else {
						KMyMoneyAccountsTreeModel.KMyMoneyAccountTreeEntry entry
								= (KMyMoneyAccountsTreeModel.KMyMoneyAccountTreeEntry)
								path.getLastPathComponent();
						setSelectedAccount(entry.getAccount());
					}

				}
			});

		}
		
		return accountsTree;
	}

	/**
	 * This method initializes transactionsPanel.
	 *
	 * @return javax.swing.JTable
	 */
	protected JTabbedPane getJTabbedPane() {
		if (myTabbedPane == null) {
			myTabbedPane = new JTabbedPane();
			myTabbedPane.addTab(Messages_JKMyMoneyViewer.getString("JKMyMoneyViewer.1"), getTransactionsPanel());
		}
		
		return myTabbedPane;
	}

	/**
	 * This method initializes transactionsPanel.
	 *
	 * @return javax.swing.JTable
	 */
	protected TransactionsPanel getTransactionsPanel() {
		if (transactionsPanel == null) {
			transactionsPanel = new TransactionsPanel();
			transactionsPanel.setSplitActions(getSplitActions());
		}
		
		return transactionsPanel;
	}

	/**
	 * The currently selected account.
	 *
	 * @return the selectedAccount
	 */
	public KMyMoneyAccount getSelectedAccount() {
		return selectedAccount;
	}

	/**
	 * The currently selected account.
	 *
	 * @param aSelectedAccount the selectedAccount to set (may be null)
	 */
	public void setSelectedAccount(final KMyMoneyAccount aSelectedAccount) {
		selectedAccount = aSelectedAccount;

		getTransactionsPanel().setAccount(selectedAccount);
		if (selectedAccount != null) {
			LOGGER.debug("setSelectedAccount: Account " + selectedAccount.getID()
					+ " = " + selectedAccount.getQualifiedName()
					+ " selected");
		}
	}

	/**
	 * This method initializes jJMenuBar.
	 *
	 * @return javax.swing.JMenuBar
	 */
	@Override
	public JMenuBar getJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getFileMenu());
		}
		
		return jJMenuBar;
	}

	/**
	 * This method initializes FileMenu.
	 *
	 * @return javax.swing.JMenu
	 */
	protected JMenu getFileMenu() {
		if (myFileMenu == null) {
			myFileMenu = new JMenu();
			myFileMenu.setText(Messages_JKMyMoneyViewer.getString("JKMyMoneyViewer.3"));
			myFileMenu.setMnemonic('f');
			myFileMenu.add(getFileLoadMenuItem());
			myFileMenu.add(new JSeparator());
			myFileMenu.add(getFileExitMenuItem());
		}
		
		return myFileMenu;
	}


	/**
	 * This method initializes FileLoadMenuItem.
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getFileLoadMenuItem() {
		if (myFileLoadMenuItem == null) {
			myFileLoadMenuItem = new JMenuItem();
			myFileLoadMenuItem.setText(Messages_JKMyMoneyViewer.getString("JKMyMoneyViewer.4"));
			myFileLoadMenuItem.setMnemonic('a');
			myFileLoadMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					JKMyMoneyViewer.this.loadFile();
				}
			});
		}
		
		return myFileLoadMenuItem;
	}

	/**
	 * This method initializes fileExitMenuItem.
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getFileExitMenuItem() {
		if (myFileExitMenuItem == null) {
			myFileExitMenuItem = new JMenuItem();
			myFileExitMenuItem.setText(Messages_JKMyMoneyViewer.getString("JKMyMoneyViewer.5"));
			myFileExitMenuItem.setMnemonic('x');
			myFileExitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					doExit();
				}
			});
		}
		
		return myFileExitMenuItem;
	}


	/**
	 * This method initializes jContentPane.
	 *
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			final int border = Const.PANE_BORDER_DEFAULT;
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(
					border, border, border, border));
			jContentPane.add(getJSplitPane(), java.awt.BorderLayout.CENTER);
		}
		
		return jContentPane;
	}

	/**
	 * This method initializes this gui.
	 */
	protected void initializeGUI() {
		this.setJMenuBar(getJMenuBar());
		this.setContentPane(getJContentPane());
		this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		this.setTitle(TITLE);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(final java.awt.event.WindowEvent e) {
				doExit();
			}
		});

	}

	/**
	 * This method initializes treeScrollPane.
	 *
	 * @return javax.swing.JScrollPane
	 */
	private javax.swing.JScrollPane getTreeScrollPane() {
		if (treeScrollPane == null) {
			final int defaultWidth = Const.SCROLL_PANE_WIDTH_DEFAULT;
			treeScrollPane = new JScrollPane();
			treeScrollPane.setViewportView(getAccountsTree());
			treeScrollPane.setPreferredSize(new Dimension(defaultWidth, Integer.MAX_VALUE));
		}
		
		return treeScrollPane;
	}

	/**
	 * This method initializes jFileChooser.
	 * If is used for the open-dialog.
	 * In JKMyMoney it is also used for the save,
	 * save as and import -dialog.
	 *
	 * @return javax.swing.JFileChooser
	 */
	protected javax.swing.JFileChooser getJFileChooser() {
		if (jFileChooser == null) {
			jFileChooser = new javax.swing.JFileChooser();
		}
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				return true; // accept all files
			}

			@Override
			public String getDescription() {
				return Messages_JKMyMoneyViewer.getString("JKMyMoneyViewer.8");
			}
		});
		
		return jFileChooser;
	}

	/**
	 * Given a file, create a KMyMoneyFile for it.
	 *
	 * @param f the file
	 * @return the KMyMoneyFile
	 * @throws IOException   if the file cannot be loaded from disk
	 */
	protected KMyMoneyFile createModelFromFile(final File f) throws IOException {
		return new KMyMoneyFileImpl(f);
	}

	/**
	 * @return true if the file was loaded successfully
	 */
	protected boolean loadFile() {
		int state = getJFileChooser().showOpenDialog(this);
		if (state == JFileChooser.APPROVE_OPTION) {
			File f = getJFileChooser().getSelectedFile();
			if (f == null) {
				return false;
			}
			if (!f.exists()) {
				JOptionPane.showMessageDialog(JKMyMoneyViewer.this, 
											  Messages_JKMyMoneyViewer.getString("JKMyMoneyViewer.6"), 
											  Messages_JKMyMoneyViewer.getString("JKMyMoneyViewer.7"), 
											  JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return loadFile(f);
		}
		return false;
	}

	/**
	 * @param f the file to load.
	 * @return true if the file was loaded successfully
	 */
	public boolean loadFile(final File f) {
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			setModel(createModelFromFile(f));
			return true;
		} catch (Exception e1) {
			LOGGER.error("loadFile: Cannot load file '" + f.getAbsoluteFile() + "'", e1);
			e1.printStackTrace();
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
		
		return false;
	}

	/**
	 * Exit the JVM.
	 */
	protected void doExit() {
		System.exit(0);
	}

	/**
	 * @return the file we operate on.
	 */
	protected KMyMoneyFileImpl getModel() {
		return myModel;
	}

	public void setModel(final KMyMoneyFile model) throws IOException {
		if ( model == null ) {
			throw new IllegalArgumentException("argument <model> is null");
		}

		myModel = new KMyMoneyFileImpl( model );
		getAccountsTree().setModel(new KMyMoneyAccountsTreeModel(myModel));
		setSelectedAccount(null);
		setTitle(TITLE);
	}
	
	/**
	 * @param model the file we operate on.
	 */
	public void setModel(final KMyMoneyFileImpl model) {
		if ( model == null ) {
			throw new IllegalArgumentException("argument <model> is null");
		}

		myModel = model;
		getAccountsTree().setModel(new KMyMoneyAccountsTreeModel(myModel));
		setSelectedAccount(null);
		setTitle(TITLE);
	}

	/**
	 * @return the accountTreePopupMenu
	 */
	protected JPopupMenu getAccountTreePopupMenu() {
		if ( myAccountTreePopupMenu == null ) {
			myAccountTreePopupMenu = new JPopupMenu();
			Collection<AccountAction> accountActions = getAccountActions();
			for ( AccountAction accountAction2 : accountActions ) {
				final AccountAction accountAction = accountAction2;
				JMenuItem newMenuItem = new JMenuItem(new AccountActionWrapper(accountAction));
				myAccountTreePopupMenu.add(newMenuItem);
			}
			LOGGER.debug("getAccountTreePopupMenu: Created menu with " + myAccountTreePopupMenu.getComponentCount() + " entries");

		}
		
		int count = myAccountTreePopupMenu.getComponentCount();
		for ( int i = 0; i < count; i++ ) {
			Component component = myAccountTreePopupMenu.getComponent(i);
			if ( component instanceof JMenuItem ) {
				JMenuItem item = (JMenuItem) component;
				Action action = item.getAction();
				if ( action instanceof AccountActionWrapper ) {
					AccountActionWrapper wrapper = (AccountActionWrapper) action;
					wrapper.getAccountAction().setAccount(getSelectedAccount());
					wrapper.setEnabled(wrapper.isEnabled());
				}
			}
		}
		
		return myAccountTreePopupMenu;
	}

	/**
	 * @return the {@link AccountAction} we have
	 */
	protected Collection<AccountAction> getAccountActions() {
		if (myAccountActions == null) {
			myAccountActions = new LinkedList<AccountAction>();
			myAccountActions.add(new OpenAccountInNewTab(getJTabbedPane()));
			myAccountActions.add(new OpenAccountInNewWindow());
		}
		return myAccountActions;
	}

	/**
	 * @return the {@link AccountAction} we have
	 */
	protected Collection<TransactionSplitAction> getSplitActions() {
		if (mySplitActions == null) {
			mySplitActions = new LinkedList<TransactionSplitAction>();
			mySplitActions.add(new OpenAccountInNewTab(getJTabbedPane()));
			mySplitActions.add(new OpenAccountInNewWindow());
		}
		LOGGER.debug("getSplitActions: JKMyMoneyViewer has " + (mySplitActions == null ? "no" : mySplitActions.size()) + " split-actions");
		return mySplitActions;
	}

	/**
	 * @param account the account to show
	 */
	public void openAccountInTab(final KMyMoneyAccount account) {
		final TransactionsPanel newTransactionsPanel = new TransactionsPanel();
		newTransactionsPanel.setAccount(account);
		String tabName = account.getName();
		addTab(tabName, newTransactionsPanel);
	}

	/**
	 * @param tabName    the label of the tab
	 * @param tabContent the content
	 */
	private void addTab(final String tabName, final JComponent tabContent) {
		final JTabbedPane tabbedPane = getJTabbedPane();
		tabbedPane.addTab(null, tabContent);
		JPanel tab = new JPanel(new BorderLayout(2, 0));
		tab.setOpaque(false);
		tab.add(new JLabel(tabName), BorderLayout.CENTER);
		JButton closeButton = new JButton("X");
		closeButton.setBorder(BorderFactory.createEmptyBorder());
		final int size = 10;
		closeButton.setPreferredSize(new Dimension(size, size));
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent aE) {
				tabbedPane.remove(tabContent);
			}

		});
		tab.add(closeButton, BorderLayout.EAST);
		tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tab);
	}

}
