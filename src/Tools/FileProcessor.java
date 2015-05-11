package Tools;

import Model.Containers.Panels.MusicPanel;
import Main.Main;
import Model.Staff.Staff;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileProcessor {
	
	public static void savePNG ( File f, MusicPanel albert ) {
	    if (albert == null) out("Что ты пытаешься сохранить, мудак?!");
	    BufferedImage img = new BufferedImage(albert.getWidth(),albert.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    Graphics g = img.createGraphics();
	    g.setColor(Color.GREEN);
	    g.fillRect(15,15,80,80);
	    albert.paintComponent(g);
	
	    try {
	        ImageIO.write(img, "png", f);
	    } catch (IOException e) {
	        out("Ошибка рисования");
	    }
	}
	
	private static void out(String str) {
	    System.out.println(str);
	}

	public static int saveJsonFile(File f, MusicPanel musicPanel) {
		Staff stan = musicPanel.getStaff();
		try {
			JSONObject js = new JSONObject("{}");
			js.put("stanExternalRepresentation", stan.getJsonRepresentation());
			try {
				PrintWriter out = new PrintWriter(f);
				out.println(js.toString(2));
				out.close();
				Main.window.setTitle(f.getAbsolutePath());
				musicPanel.getStoryspaceScroll().setTitle(f);
			} catch (IOException exc) { System.out.println("У нас тут жорпа с файлом " ); exc.printStackTrace(); }
		} catch (JSONException exc) { System.out.println("У нас тут жорпа с жсоном " ); exc.printStackTrace(); }
		return 0;
	}

	public static int openJsonFile( File f, MusicPanel musicPanel ) {
		try {
			byte[] encoded = Files.readAllBytes(f.toPath());
			String js = new String(encoded, StandardCharsets.UTF_8);
			try {
				JSONObject jsObject = new JSONObject(js);
				musicPanel.getStaff().reconstructFromJson(jsObject.getJSONObject("stanExternalRepresentation"));
				Main.window.setTitle(f.getAbsolutePath());
				musicPanel.getStoryspaceScroll().setTitle(f);
			} catch (Exception exc) { System.out.println("У нас тут жорпа с открытием жсона " ); exc.printStackTrace(); }
		} catch (IOException exc) { System.out.println("Жопа при открытии жс файла"); exc.printStackTrace(); }
		return 0;
	}
}
