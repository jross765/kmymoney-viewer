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

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.viewer.actions.AccountAction;
import org.kmymoney.viewer.actions.OpenAccountInNewTab;
import org.kmymoney.viewer.actions.OpenAccountInNewWindow;
import org.kmymoney.viewer.actions.TransactionSplitAction;
import org.kmymoney.viewer.models.KMyMoneyAccountsTreeModel;
import org.kmymoney.viewer.models.KMyMoneyFileImpl;
import org.kmymoney.viewer.panels.TransactionsPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Viewer for KMyMoney files.
 */
@SuppressWarnings("serial")
public class JKMyMoneyViewer extends JFrame {

	private static final String TITLE = "JKMyMoney Viewer";

	private static final int DEFAULT_WIDTH  = 750;
	private static final int DEFAULT_HEIGHT = 600;

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
			}
			catch (Exception e) {
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
			}
			catch (Exception e) {
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

	/**
	 * Our logger for debug- and error-output.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JKMyMoneyViewer.class);

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

	/**
	 * @param args empty or contains a gnucash-file-name as a first param.
	 */
	public static void main(final String[] args) {
		JKMyMoneyViewer ste = new JKMyMoneyViewer();
		installNimbusLaF();
		ste.initializeGUI();
		ste.setVisible(true);
		if (args.length > 0) {
			ste.loadFile(new File(args[0]));
		}
		ste.getJSplitPane().setDividerLocation(0.3);
	}

	protected static void installNimbusLaF() {
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		}
		catch (UnsupportedLookAndFeelException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * This method initializes
	 * the KMyMoneyViewer.
	 */
	public JKMyMoneyViewer() {
		super();
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
		}
		catch (Exception e1) {
			LOGGER.error("loadFile: Cannot load file '" + f.getAbsoluteFile() + "'", e1);
			e1.printStackTrace();
		}
		finally {
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
