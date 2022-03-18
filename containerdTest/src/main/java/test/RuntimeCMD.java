package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RuntimeCMD {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Process proc = Runtime.getRuntime().exec("classpath:your_script.cmd");
        Process process = null;
        try {
            // process = Runtime.getRuntime().exec("pwd"); // for Linux
            process = Runtime.getRuntime().exec("cmd /c dir"); //for Windows

            process.waitFor();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
