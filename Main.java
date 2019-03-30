////////
import java.util.*;
import java.io.*;

////////
public class Main {
	// ####
	public static void main(String [] args) throws IOException, InterruptedException {
		int seed = new Random().nextInt(128);
		Random rand = new Random(seed);
		HW_1700017720 tetris = new HW_1700017720();
		int score = tetris.run(rand);
		System.out.println(tetris.id+": score="+score);
	}
}


