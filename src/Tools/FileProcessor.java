package Tools;

import Gui.SheetMusic;
import Gui.staff.Staff;
import Gui.staff.pointerable.Nota;
import Gui.staff.pointerable.Phantom;
import Gui.staff.Pointer;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Dictionary;

public class FileProcessor {
	
	public static void savePNG ( File f, Staff staff ) {
	    SheetMusic albert = staff.parentSheetMusic;
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

	public static int saveJsonFile( File f, Staff stan ) {
		try {
			JSONObject js = new JSONObject("{}");
			js.put("stanExternalRepresentation", stan.getExternalRepresentation());
			try {
				PrintWriter out = new PrintWriter(f);
				out.println(js.toString(2));
				out.close(); 
			} catch (IOException exc) { System.out.println("У нас тут жорпа с файлом " ); exc.printStackTrace(); }
		} catch (JSONException exc) { System.out.println("У нас тут жорпа с жсоном " ); exc.printStackTrace(); }
		return 0;
	}

	public static int openJsonFile( File f, Staff stan ) {
		try {
			byte[] encoded = Files.readAllBytes(f.toPath());
			String js = new String(encoded, StandardCharsets.UTF_8);
			try {
				JSONObject jsObject = new JSONObject(js);
				stan.reconstructFromJson(jsObject.getJSONObject("stanExternalRepresentation"));
			} catch (Exception exc) { System.out.println("У нас тут жорпа с открытием жсона " ); exc.printStackTrace(); }
		} catch (IOException exc) { System.out.println("Жопа при открытии жс файла"); exc.printStackTrace(); }
		return 0;
	}
}
