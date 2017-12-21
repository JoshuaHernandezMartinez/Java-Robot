import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class RobotPlayer implements Runnable{
	
	private int redSauceX = 283, redSauceY = 336;
	private int sausageX = 818, sausageY = 480;
	private int breadX = 468, breadY = 473;
	private int b1x = 465, b2x = 506, b3x = 541;
	private int s1x = 781, s2x = 814, s3x = 842;
	private int b_s_y = 403;
	private int m1 = 340, m2 = 452, m3 = 579, m4 = 690, m5 = 815;
	private int m_y = 332;
	
	private int h1 = 0, h2 = 0, h3 = 0; // hotdogs status // 0 empty 1 not empty
	
	private int hotdogs = 0;
	private int sausages = 0;
	
	private BufferedImage hotdog_sauce;
	private BufferedImage hotdog;
	
	private Rectangle foodRequestZone;
	private BufferedImage screen;
	private Robot r;
	
	private Order[] orders;
	
	private boolean running;
	private Thread thread;
	
	private long t, dt;
	private long orders_time = 0;
	private long cook_time = 0;
	
	private boolean cooking = false;
	
	private int COOK_TIME = 8000;
	private int TAKE_ORDERS_TIME = 2000;
	
	public RobotPlayer() {
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		hotdog_sauce = Loader.loadImage("/hotdog_sauce.png");
		hotdog = Loader.loadImage("/hotdog.png");
		
		foodRequestZone = new Rectangle(270, 140, 580, 70);
		screen = r.createScreenCapture(foodRequestZone);
		orders = new Order[5];
		
		t = System.currentTimeMillis();
		dt = 0;
		
	}
	
	@Override
	public void run() {
		
		while(running) {
			
			dt = System.currentTimeMillis() - t;
			t = System.currentTimeMillis();
			orders_time += dt;
			if(cooking) {
				cook_time += dt;
			}else {
				cook_time = 0;
			}
			
			take_orders();
			prepare_food();
			
			// check if the mouse x is outside the game area
			// this means I want to stop the program
			
			Point p = MouseInfo.getPointerInfo().getLocation();
			
			if(p.y > 600)
				running = false;
			
		}
		
		System.out.println("Program Ended");
		
		stop();
	}
	
	private void take_orders() {
		
		// take customers orders
		
		if(orders_time >= TAKE_ORDERS_TIME) {
			
			screen = r.createScreenCapture(foodRequestZone);
			
			ArrayList<Point> pts = imageOnScreen(hotdog_sauce);
			
			if(pts.size() > 0) {	
				
				for(int i = 0; i < pts.size(); i++) {
					int index = pts.get(i).x / 116;
					if(orders[index] == null) {
						orders[index] = new Order(0, true);
					}
				}
			}
			
			pts = imageOnScreen(hotdog);
			
			if(pts.size() > 0) {	
				
				for(int i = 0; i < pts.size(); i++) {
					int index = pts.get(i).x / 116;
					if(orders[index] == null) {
						orders[index] = new Order(0, false);
					}
				}
			}
			
			orders_time = 0;
			
		}
		
		// deliver orders if possible && take cash
			
		for(int i = 0; i < 5; i++) {
			
			Order o = orders[i];
			
			if(o == null)
				continue;
			
			if(o.food_type == 0) {
				deliver_hotdog(i, o.red_sauce);
				orders[i] = null;
			}
		}
	}
	
	private void deliver_hotdog(int index, boolean red_sauce) {
		int sx = 0;
		
		if(hotdogs == 3) {
			sx = b3x;
			h3 = 0;
		}else if(hotdogs == 2) {
			sx = b2x;
			h2 = 0;
		}else if(hotdogs == 1) {
			sx = b1x;
			h1 = 0;
		}else if(hotdogs == 0){
			return;
		}else {
			System.out.println("ERROR: hotdogs: "+hotdogs);
			running = false;
		}
		
		if(red_sauce) {
			move_mouse(redSauceX, redSauceY);
			press_mouse_left(false);
			move_mouse(sx, b_s_y);
			release();
		}else {
			move_mouse(sx, b_s_y);
		}
		
		press_mouse_left(false);
		int x = foodRequestZone.x + index * 116 + 58;
		int y = foodRequestZone.y + foodRequestZone.height / 2;
		move_mouse(x, y);
		release();
		if(hotdogs > 0)
			hotdogs -= 1;
		int money_x = 0;
		if(index == 0) {
			money_x = m1;
		}else if(index == 1) {
			money_x = m2;
		}else if(index == 2) {
			money_x = m3;
		}else if(index == 3) {
			money_x = m4;
		}else if(index == 4) {
			money_x = m5;
		}
		
		move_mouse(money_x, m_y);
		press_mouse_left(true);
		
	}
	
	private void prepare_food() {
		if(!cooking && cook_time == 0) {
			move_mouse(breadX, breadY);
			for(int i = 0; i < 3; i++) {
				press_mouse_left(true);
			}
			
			move_mouse(sausageX, sausageY);
			for(int i = 0; i < (3 - hotdogs); i++) {
				press_mouse_left(true);
				sausages ++;
			}
			cooking = true;
		}
		
		if(cook_time > COOK_TIME && cooking) {
			
			if(h3 == 0) {
				put_sausage(s3x, b3x);
				put_sausage(s2x, b3x);
				put_sausage(s1x, b3x);
				h3 = 1;
			}
			if(h2 == 0) {
				put_sausage(s3x, b2x);
				put_sausage(s2x, b2x);
				put_sausage(s1x, b2x);
				h2 = 1;
			}
			if(h1 == 0) {
				put_sausage(s3x, b1x);
				put_sausage(s2x, b1x);
				put_sausage(s1x, b1x);
				h1 = 1;
			}
			
			hotdogs += sausages;
			sausages = 0;
			
			cook_time = 0;
			cooking = false;
			
		}
	}
	
	private void put_sausage(int s_pos, int b_pos) {
		move_mouse(s_pos, b_s_y);
		press_mouse_left(false);
		move_mouse(b_pos, b_s_y);
		release();
	}
	
	public ArrayList<Point> imageOnScreen(BufferedImage img) {
		
		ArrayList<Point> p = new ArrayList<Point>();
		
		for(int x = 0; x < screen.getWidth() - img.getWidth(); x++) {
			tag:
			for(int y = 0; y < screen.getHeight() - img.getHeight(); y++) {
				
				for(int _x = 0; _x < img.getWidth(); _x++) {
					for(int _y = 0; _y < img.getHeight(); _y++) {
						if((screen.getRGB(x + _x, y + _y)
								!= img.getRGB(_x, _y))) {
							continue tag;
						}
					}
				}
				p.add(new Point(x, y));
			}
		}
		
        return p;
	}
	
	private void move_mouse(int x, int y) {
		r.mouseMove(x, y);
		r.delay(20);
	}
	
	private void press_mouse_left(boolean rls) {
		r.mousePress(InputEvent.BUTTON1_MASK);
		r.delay(20);
		if(rls) {
			release();
		}
	}
	
	private void release() {
		r.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	public void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}
	
	private void stop() {
		try {
			thread.join();
			running = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
