package edu.ucsb.cs56.projects.games.poker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

final class PokerSinglePlayer extends PokerGame {

    Timer timer;

    public PokerSinglePlayer() {
    }
    
    public PokerSinglePlayer(int dChips, int pChips){
	dealer.setChips(dChips);
	player.setChips(pChips);
    }
    
    public void go() {
	pot = 0;
	layoutSubViews();
	if(!gameOver){
	    step = Step.BLIND;
	    turn = Turn.DEALER;
	    timer = new Timer(2000, new ActionListener() {
		    public void actionPerformed(ActionEvent e){
			dealerAI();
		    }
		});
	    timer.setRepeats(false);
	    message = "Dealer is thinking...";
	    prompt = "Dealer goes first!";
	    timer.restart();
	}
    }

    /**
     *  Overwritten method to activate the dealer AI on turn change.
    */
    public void changeTurn() {
	if(turn == Turn.PLAYER){
	    if(responding == true){
		turn = Turn.DEALER;
		controlButtons();
		updateFrame();
		message = "Dealer is thinking...";
		timer.restart();
	    }
	    else{
		updateFrame();
		nextStep();
		if(step != Step.SHOWDOWN){
		    turn = Turn.DEALER;
		    controlButtons();
		    prompt = "Dealer turn.";
		    message = "Dealer is thinking...";
		    updateFrame();
		    timer.restart();
		}
	    }
	}
	else if(turn == Turn.DEALER){
	    if(responding == true){
		turn = Turn.PLAYER;
		controlButtons();
		responding = false;
		prompt = "What will you do?";
		updateFrame();
	    }
	    else {
		prompt = "What will you do?";	
		turn = Turn.PLAYER;
		controlButtons();
		updateFrame();
	    }
	}
    }

    /**
     *  Simple method called in single player to play the dealer's turn.
     *  Currently the dealer will only check or call.
     */
    public void dealerAI () {
	Hand dealerHand = new Hand();
	if (step == Step.BLIND) {
	    if(dealerHand.size() != 2){
		for(int i =0; i<2; i++) {
		    dealerHand.add(dealer.getHand().get(i));
		}
	    }
	}
	else if(step == Step.FLOP) {
	    if(dealerHand.size() < 5){
		for(Card c : flop) {
		    dealerHand.add(c);
		}
	    }
	}
	else if(step == Step.TURN) {
	    if(dealerHand.size() < 6){
		dealerHand.add(turnCard);
	    }
	}
	else if(step == Step.RIVER) {
	    if (dealerHand.size() < 7) {
		dealerHand.add(riverCard);
	    }
	}
	else {}

	boolean shouldBet = false;
	boolean shouldCall = true;
	int dValue = dealerHand.calculateValue();
	int betAmount = 5*dValue;
	if(step == Step.BLIND) {
	    if(dValue >= 1){
		shouldBet = true;
	    }
	}
	else if(step == Step.FLOP) {
	    if(dValue >= 3){
		shouldBet = true;
	    }
	    if((dValue == 0 && bet >= 20) ){
		shouldCall = false;
	    }
	}
	else if(step == Step.TURN) {
	    if(dValue >= 4){
		shouldBet = true;
	    }
	    if((dValue < 2 && bet > 20)) {
		shouldCall = false;
	    }
	}
	else if(step == Step.RIVER) {
	    if(dValue >= 4){
		shouldBet = true;
	    }
	    if((dValue < 2 && bet > 20))
		shouldCall = false;
	}
	
	if(responding == true){
	    if(shouldCall) {
		message = "Dealer calls.";
		pot += bet;
		dealer.setChips(dealer.getChips() - dealer.bet(bet));
		bet = 0;
		responding = false;
		nextStep();
		updateFrame();
		timer.restart();
	    }
	    else{
		message = "Dealer folds.";
		dealer.foldHand();
	    }
	}
	else if(shouldBet && step != Step.SHOWDOWN) {
	    if((dealer.getChips() - betAmount >= 0) && (player.getChips()-betAmount >=0)){
		bet = betAmount;
		pot += bet;
		dealer.setChips(dealer.getChips() - dealer.bet(bet));
		responding = true;
		message = "Dealer bets " + bet + " chips.";
		updateFrame();
		changeTurn();
	    }
	    else{
		message = "Dealer checks.";
		updateFrame();
		changeTurn();
	    }
	}
	else if(step != Step.SHOWDOWN){
	    message = "Dealer checks.";
	    updateFrame();
	    changeTurn();
	}
    }

    /**
     * Method overridden to allow for a new single player game to start.
     */
    
    public void showWinnerAlert() {
	if(!gameOver){
	    String message = "";
	    dSubPane2.remove(backCardLabel1);
	    dSubPane2.remove(backCardLabel2);
	    for(int i=0;i<2;i++){
		dSubPane2.add(new JLabel(getCardImage(dealer.getCardFromHand(i))));
	    }
	    updateFrame();
	    if (winnerType == Winner.PLAYER) {
            System.out.println("player");
            message = "You won! \n\n Next round?";
	    } else if (winnerType == Winner.DEALER) {
		System.out.println("dealer");
		message = "Dealer won. \n\n Next round?";
	    } else if (winnerType == Winner.TIE){
            System.out.println("tie");
            message = "Tie \n\n Next round?";
	    }
	    
	    int option = JOptionPane.showConfirmDialog(null, message, "Winner",
						       JOptionPane.YES_NO_OPTION);
	    if (option == JOptionPane.YES_OPTION) {
		// Restart
		mainFrame.dispose();
		// Create new game
		PokerSinglePlayer singlePlayerReplay = new PokerSinglePlayer();
		singlePlayerReplay.go();
	    } else {
		// Quit
		System.exit(1);
	    }
	}
	else{
	    gameOverFrame = new JFrame();
	    gameOverFrame.setLayout(new FlowLayout());
	    gameOverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    
	    gameOverButton = new JButton("Back to Main Menu");
	    
	    gameOverButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e){
			gameOverFrame.setVisible(false);
			PokerMain restart = new PokerMain();
			restart.go();
		    }
		});
	    if(dealer.getChips() < 5) {
		gameOverLabel = new JLabel("GAME OVER!\n\n Dealer has run of of chips!");
	    }
	    else
		gameOverLabel = new JLabel("GAME OVER!\n\n You have run of of chips!");
	    gameOverFrame.getContentPane().add(gameOverLabel);
	    gameOverFrame.getContentPane().add(gameOverButton);
	    gameOverFrame.setSize(400,200);
	    gameOverFrame.setLocation(250, 250);
	    
	    gameOverFrame.setVisible(true);
	}
    }
    
}