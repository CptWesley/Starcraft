package eisbw.debugger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import eisbw.Game;

/**
 * @author Danny & Harm.
 */
public class DrawButtons extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private final Game game;
	private final Color buttonBackground;

	/**
	 * Toggle switches to draw in the game.
	 *
	 * @param game
	 *            - the game data.
	 */
	public DrawButtons(Game game) {
		this.game = game;
		setLayout(new BorderLayout());

		JLabel label = new JLabel("Draw actions", SwingConstants.CENTER);
		add(label, BorderLayout.NORTH);

		JButton mapButton = new JButton("Map info");
		mapButton.setActionCommand(Draw.MAP.name());
		mapButton.addActionListener(this);
		JButton unitButton = new JButton("Unit info");
		unitButton.setActionCommand(Draw.UNITS.name());
		unitButton.addActionListener(this);

		this.buttonBackground = mapButton.getBackground();

		JPanel drawPanel = new JPanel();
		drawPanel.add(mapButton);
		drawPanel.add(unitButton);
		add(drawPanel);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		this.game.toggleDraw(event.getActionCommand());
		JButton buttonPressed = (JButton) event.getSource();
		if (buttonPressed.getBackground().equals(Color.GRAY)) {
			buttonPressed.setBackground(this.buttonBackground);
		} else {
			buttonPressed.setBackground(Color.GRAY);
		}
	}
}
