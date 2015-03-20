package Gui;

import Musica.*;
import Pointerable.Accord;
import Pointerable.IAccord;
import Pointerable.Nota;
import Pointerable.Phantom;
import Pointerable.Pointer;
import Pointerable.Pointerable;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;


public class SheetMusic extends JPanel {
	JScrollPane scroll;
	
	public static int notaHeight = 32;
	public static int NORMAL_HEIGHT = 40;
	public static int notaWidth = 20;
	public static int NORMAL_WIDTH = 25;
	public static int STEPY = notaHeight/8; // Графика // TODO: move it into Constants class maybe?
	public static int STEPX = notaWidth; // Графика // TODO: move it into Constants class maybe?
	double MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек // TODO: move it into Constants class maybe?
	double MARGIN_H = 1; // TODO: move it into Constants class maybe?
	int MARX = (int)Math.round(MARGIN_H* STEPX);
	int MARY = (int)Math.round(MARGIN_V* STEPY);
	int toOtGraph = 38* STEPY;
	int maxy = 0;
	int SISDISPLACE = 40;
	
	public static BufferedImage[] vseKartinki = new BufferedImage[6]; // TODO: поменять этот уродский массив на ключ-значение // Гузно у тебя уродское
	public static BufferedImage[] vseKartinki0 = new BufferedImage[6];
	GeneralPath triang;
	
	Staff stan;
	ArrayList<Staff> staffList = new ArrayList();
	
	public void incScale(int n) {
	    notaHeight += 8*n;
	    notaWidth += 5*n;
	    if (notaHeight < 16 || notaWidth < 10) {
	        notaHeight = 16;
	        notaWidth = 10;
	    }
	    if (notaHeight > NORMAL_HEIGHT || notaWidth > NORMAL_WIDTH) {
	        notaHeight = NORMAL_HEIGHT;
	        notaWidth = NORMAL_WIDTH;
	    }
	    refresh();
	}
	
	public void refresh() {
	    STEPY = notaHeight/8;
	    STEPX = notaWidth;
	    for (int i = 0; i < vseKartinki.length; ++i ) { // >_<
	        vseKartinki[i] = changeSize(i);
	    }
	    MARX = (int)Math.round(MARGIN_H* STEPX);
	    MARY = (int)Math.round(MARGIN_V* STEPY);
	    toOtGraph = 38* STEPY;
	
	    Nota.refreshSizes();
	    maxy = 0;
	    repaint();
	}
	
	private BufferedImage changeSize(int idx) { // TODO
	    int w0 = vseKartinki0[idx].getWidth();
	    int h0 = vseKartinki0[idx].getHeight();
	    int w1 = w0*notaWidth/NORMAL_WIDTH;
	    int h1 = h0*notaHeight/NORMAL_HEIGHT;
	
	    BufferedImage tmp = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
	    Graphics g = tmp.createGraphics();
	    Image scaledImage = vseKartinki0[idx].getScaledInstance(w1, h1, Image.SCALE_SMOOTH);
	    g.drawImage(scaledImage, 0, 0, w1, h1, null);
	    g.dispose();
	    return tmp;
	}
	
	public SheetMusic(final Staff stan) {
	    this.stan = stan;
	    URL curUr = getClass().getResource("../");
	    System.out.println(curUr.getPath());
	    String keyRes = "imgs/vio_sized.png";
	    String basRes = "imgs/bass_sized.png";
	    String bemRes = "imgs/flat_sized.png";
	    String ptrRes = "imgs/MyPointer.png";
	    String volRes = "imgs/volume.png";
		String instrRes = "imgs/instrument.png";
	    try {	vseKartinki[0] = ImageIO.read(new File(keyRes));
				vseKartinki[1] = ImageIO.read(new File(basRes));
				vseKartinki[2] = ImageIO.read(new File(bemRes));
				vseKartinki[3] = ImageIO.read(new File(ptrRes));
				vseKartinki[4] = ImageIO.read(new File(volRes));
				vseKartinki[5] = ImageIO.read(new File(instrRes));
	    } catch (IOException e) { e.printStackTrace(); System.out.println("Темнишь что-то со своей картинкой..."); }
	    for (int i = 0; i < vseKartinki.length; ++i ) {
	        vseKartinki0[i] = vseKartinki[i];
	        vseKartinki[i] = changeSize(i);
	    }
	
	    int xPoints[] = { 6, 11, 16 };
	    int yPoints[] = { 0, 25, 0 };
	    triang = new GeneralPath();
	    triang.moveTo( xPoints[ 0 ], yPoints[ 0 ] );
	    for ( int k = 1; k < xPoints.length; k++ )
	        triang.lineTo( xPoints[ k ], yPoints[ k ] );
	    triang.closePath();
	    stan.drawPanel = this;
	}

	int taktCount = 1;
	int curAccord = -2;
	
	int triol = 0;
	int triolTaktSum = 0;
	
	int maxSys = 1;
	int lastMaxSys = 1;
	boolean trolling = false;
	
	synchronized public void paintComponent(Graphics g) {
		triolTaktSum = 0;

		for (Staff stave: this.getStaveList()) {
			g.setColor(Color.WHITE);
			g.fillRect(0,0,this.getWidth(),this.getHeight());
			g.setColor(Color.BLUE);
			g.drawImage(vseKartinki[1], STEPX, 11* STEPY + MARY +2, this);   //bass
			g.drawImage(vseKartinki[0], STEPX, MARY - 3* STEPY, this);       //violin
			int gPos = MARX + 4 * STEPX -2* STEPX;

			taktCount = 1;
			maxSys = 0;

			int curCislic = 0;
			int curZnamen = 1;

			drawPhantom((Phantom)(stave.getPhantom()), g, gPos, MARY);
			gPos += 2* STEPX * stave.getPhantom().getWidth();
			for (IAccord anonimus: stave.getAccordList()) {
				// Рисуем нотный стан
				if (gPos >= getStepInOneSysCount() * STEPX) {
					// Переходим на новую октаву
					++maxSys;
					g.drawImage(vseKartinki[1], STEPX, (SISDISPLACE+11)* STEPY + MARY +2, this);
					g.drawImage(vseKartinki[0], STEPX, MARY + (SISDISPLACE-3)* STEPY, this);
					for (int j=0; j<(stan.bassKey? 11: 5); ++j){
						if (j == 5) continue;
						g.drawLine(MARX, MARY + j* STEPY *2, this.getWidth() - MARX*2, MARY + j* STEPY *2);
					}


					gPos = MARX + 4 * STEPX;
					MARY += SISDISPLACE* STEPY;

				}

				if (Pointer.pointsAt == anonimus) { g.drawImage( this.getPointerBitmap(), gPos, MARY- STEPY *14, this ); }   // Картинка указателя
				

				if (anonimus instanceof Accord && ((Accord)anonimus).getNotaList().size() > 0) {
					Accord accord = (Accord)anonimus;

					// if (theNota.isTriol) { // TODO: store this info in accord (i.e. something like tupletDenominator = 3, 7 ...)

					curCislic += accord.getShortest().getNumerator();
					curCislic = drawTaktLineIfNeeded(curCislic, curZnamen, g, gPos);

					if (accord.getHighest().isBotommedToFitSystem()) { drawText("8va", g, gPos, MARY - 4 * STEPY, Color.BLUE); }

					// TODO: i get ConcurrentModificationException sometimes. Hope, it was fixed by mine synchronized
					for (Nota tmp: accord.getNotaList()) {
						curAccord = accord.getNotaList().indexOf(tmp);
						drawNotu( (Nota)tmp, g , MARY + (accord.getHighest().isBotommedToFitSystem() ? 7 * STEPY : 0), gPos);
					}

					drawText(accord.getSlog(), g, gPos, MARY, Color.BLACK);

					gPos += 2 * STEPX * accord.getWidth();
				}

				if (maxSys != lastMaxSys) {
					this.setPreferredSize(new Dimension(this.getWidth() - 20, maxy+SISDISPLACE*STEPY));	//	Needed for the scroll bars to appear
					lastMaxSys = maxSys;
				}
			}
			
			for (int i=0; i<(stan.bassKey? 11: 5); ++i){
				if (i == 5) continue;
				g.drawLine(MARX, MARY + i* STEPY *2, this.getWidth() - MARX*2, MARY + i* STEPY *2);
			}

			if (MARY>maxy) maxy=MARY;
			MARY = (int)Math.round(MARGIN_V* STEPY);
		}
		
		this.revalidate();	//	Needed to recalc the scroll bars
	} // paintComponent

	private static void drawText(String text, Graphics surface, int x, int y, Color color) {
		surface.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // 12 - 7px w  h == 12*4/5
		surface.setColor(Color.WHITE);
		surface.fillRect(x-2, y - 6*STEPY - 12*4/5 - 2, 7*text.length() + 4, 12*4/5 + 4);
		surface.setColor(color);
		surface.drawString(text, x, y - 6*STEPY);
		surface.setColor(Color.BLACK);
	}
	
	int tp = 0;
	
	int x0, y0, x1,y1;
	private int drawNotu(Nota theNota, Graphics g, int yIndent, int xIndent) {
	
		int thisY = yIndent + this.toOtGraph - STEPY * (theNota.getAcademicIndex() + theNota.getOctava() * 7);
		if (stan.getChannelFlag(theNota.channel) || true) { // стоит придумать, может отображать их по-другому... или показывать сбоку, какие каналы отключены... займись этим.
			if (theNota.isBemol()) {
				g.drawImage(vseKartinki[2], xIndent-(int)Math.round(0.5* STEPX), thisY + 3* STEPY +2, this);
			} // Хочу, чтобы он рисовал от ноты, поэтому не инкапсулировал бемоль // ну и мудак
			if (curAccord == Pointer.nNotiVAccorde) {
				g.drawImage(theNota.getImageColor(), xIndent, thisY, this);
			} else {
				g.drawImage(theNota.getImage(), xIndent, thisY, this);
			}
			int n = theNota.numerator;
			boolean to4ka = false;
			if (n % 3 == 0) {
				to4ka = true;
				n -= n/3;
			}
			g.setColor(Color.BLACK);
			if (to4ka) g.fillOval(xIndent + notaWidth*4/5, thisY + notaHeight*7/8, notaHeight/8, notaHeight/8);
		}
	
		if (triol == 1) {
			x0 = xIndent + (15)*notaHeight/NORMAL_HEIGHT;
			y0 = thisY;
		} else if (triol == 3) {
			drawTriolLine(x0, y0, xIndent, thisY, g);
		}

		if (theNota.isStriked()) g.drawLine(xIndent - notaWidth*4/25, thisY + STEPY * 7, xIndent + notaWidth*22/25, thisY + STEPY * 7);
	
		return 0;
	} 
	
	private void drawTriolLine(int x0, int y0, int x1, int thisY, Graphics g ) {
		x1 += (15)*notaHeight/NORMAL_HEIGHT;
		y1 = thisY;
		--x0; --x1;
		g.setColor(Color.BLACK);
		if (x1 > x0) {
			g.drawLine(x0,y0, x1,y1);
			g.drawLine(x0,y0+1, x1,y1+1);
			g.drawLine(x0,y0+2, x1,y1+2);
			g.drawString("3",x0+(x1-x0)/2 - notaWidth/2, y0+(y1-y0)/2 - notaHeight/4);
		} else {
			int length = STEPX*3;
			g.drawLine(x0,y0, x0 + length,y0);
			g.drawLine(x0,y0+1, x0 + length,y0+1);
			g.drawLine(x0,y0+2, x0 + length,y0+2);
			g.drawString("3",x0+STEPX - notaWidth/2, y0 - notaHeight/4);
			g.drawLine(x1,y1, x1 - length,y1);
			g.drawLine(x1,y1+1, x1 - length,y1+1);
			g.drawLine(x1,y1+2, x1 - length,y1+2);
			g.drawString("3",x1-STEPX - notaWidth/2, y1 - notaHeight/4);
		}
	}
	
	private void drawPhantom(Phantom phantomka, Graphics g, int xIndent, int yIndent) {
		int dX = this.getNotaWidth()/5, dY = this.getNotaHeight()*2;
		g.drawImage(phantomka.getImage(), xIndent - dX, yIndent - dY, this);
		if (phantomka.underPtr) {
			int deltaY = 0, deltaX = 0;
			switch (phantomka.changeMe) {
				case cislicelj:	deltaY += 9 * STEPY; break;
				case znamenatelj: deltaY += 11 * STEPY; break;
				case tempo: deltaY -= 1 * STEPY; break;
				case instrument: deltaY += 4 * STEPY; deltaX += STEPX / 4; break;
				case volume: deltaY += 24 * STEPY; break;
				default: out("Неизвестный енум в ДоуПанеле"); break;
			}
		    g.drawImage(this.getPointerBitmap(), xIndent - 7*notaWidth/25 + deltaX, yIndent - STEPY * 14 + deltaY, this);
		}
	}
	
	public void checkCam(){
	    JScrollBar vertical = scroll.getVerticalScrollBar();
	    vertical.setValue( SISDISPLACE* STEPY * (Pointer.pos / (getStepInOneSysCount() / 2 - 2) -1) );
	    repaint();
	}
	
	public void page(int pageCount) {
		JScrollBar vertical = scroll.getVerticalScrollBar();
		int pos = vertical.getValue()+pageCount*SISDISPLACE* STEPY;
		if (pos<0) pos = 0;
		if (pos>vertical.getMaximum()) pos = vertical.getMaximum();
		vertical.setValue(pos);
		repaint();
	}
	
	private int drawTaktLineIfNeeded(int curCislic, int curZnamen, Graphics g, int xIndent) {
		if (curCislic >= stan.cislic) {
	    	curCislic %= stan.cislic;
	        g.setColor(Color.BLACK);
	        if (curCislic > 0) {
	            g.setColor(Color.BLUE);
	        }
	        g.drawLine(xIndent + STEPX * 3 / 2, MARY - STEPY * 5, xIndent + STEPX * 3 / 2, MARY + STEPY * 20);
	        g.setColor(Color.decode("0x00A13E"));
	        g.drawString(taktCount + "", xIndent + STEPX, MARY - STEPY * 9);
	
	        ++taktCount;
		}
		return curCislic;
	}
	
	int getSys(int n){
		return n/(getStepInOneSysCount() / 2 - 2);
	}
	
	private void out(String str) {
		System.out.println(str);
	}

	// getters/setters

	public ArrayList<Staff> getStaveList() {
		ArrayList staveList = (ArrayList)this.staffList.clone();
		staveList.add(this.stan);
		return staveList;
	}

	public SheetMusic addNewStaff() {
		this.staffList.add(new Staff());
		return this;
	}

	public BufferedImage getPointerBitmap() {
		return this.vseKartinki[3];
	}

	public int getStepInOneSysCount() {
		return (int)Math.floor(this.getWidth() / this.STEPX - 2 * this.MARGIN_H);
	}

	public int getNotaWidth() {
		return this.notaWidth;
	}

	public int getNotaHeight() {
		return this.notaHeight;
	}
}
