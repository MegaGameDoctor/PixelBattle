package net.gamedoctor.pixelbattle.web;

import lombok.RequiredArgsConstructor;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.database.data.CanvasFrame;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;

@RequiredArgsConstructor
public class WebToolCommunicator {
    private final PixelBattle plugin;
    private final String url = "https://spigot.kosfarix.ru/plugins/PixelBattle/";
    private final String webToolVersion = "1.1";

    public String createCanvasImage() throws Exception {
        File tempStorage = File.createTempFile("pixelbattle", "tempData.canvas");
        tempStorage.deleteOnExit();

        BufferedWriter writer = new BufferedWriter(new FileWriter(tempStorage, true));
        for (CanvasFrame canvasFrame : plugin.getDatabaseManager().getCanvasPixelsData().values()) {
            writer.write(canvasFrame.getX() + ";" + canvasFrame.getY() + ";" + canvasFrame.getPixelData().getColor().getMaterial().toString() + ";");
            writer.newLine();
        }
        writer.close();

        HttpURLConnection httpUrlConnection = (HttpURLConnection) new URL(url + "loadData.php?type=canvas&ver=" + webToolVersion).openConnection();
        return workWithAnswer(tempStorage, httpUrlConnection);
    }

    public String createTimeLapse() throws Exception {
        File tempStorage = File.createTempFile("pixelbattle", "tempData.timelapse");
        tempStorage.deleteOnExit();

        BufferedWriter writer = new BufferedWriter(new FileWriter(tempStorage, true));
        LinkedHashMap<Integer, CanvasFrame> frames = plugin.getDatabaseManager().getFramesForTimeLapse();
        for (int frame : frames.keySet()) {
            CanvasFrame canvasFrame = frames.get(frame);
            String loc = canvasFrame.getLocation().getBlockX() + "_" + canvasFrame.getLocation().getBlockY() + "_" + canvasFrame.getLocation().getBlockZ();
            writer.write(canvasFrame.getX() + ";" + canvasFrame.getY() + ";" + loc + ";" + canvasFrame.getPixelData().getColor().getMaterial().toString() + ";");
            writer.newLine();
        }
        writer.close();

        HttpURLConnection httpUrlConnection = (HttpURLConnection) new URL(url + "loadData.php?type=timelapse&ver=" + webToolVersion).openConnection();
        return workWithAnswer(tempStorage, httpUrlConnection);
    }

    private String workWithAnswer(File tempStorage, HttpURLConnection httpUrlConnection) throws IOException {
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setRequestMethod("POST");
        OutputStream os = httpUrlConnection.getOutputStream();

        BufferedInputStream fis = new BufferedInputStream(new FileInputStream(tempStorage));

        long totalByte = fis.available();
        for (int i = 0; i < totalByte; i++) {
            os.write(fis.read());
        }

        os.close();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        httpUrlConnection.getInputStream()));

        String s = in.readLine();
        in.close();
        fis.close();
        tempStorage.delete();
        return s;
    }

    public String formatAnswer(String answer) {
        if (answer == null) {
            answer = "ERROR";
        } else if (answer.startsWith("MSG: ")) {
            answer = answer.replaceFirst("MSG: ", "");
        } else {
            answer = url + answer;
        }
        return answer;
    }
}
