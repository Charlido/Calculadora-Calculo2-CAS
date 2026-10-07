package controller;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class GestorAudio {
    private Clip clipClick;

    public GestorAudio() {
        try {
            System.out.println("Intentando localizar el archivo de audio...");
            InputStream audioSrc = getClass().getResourceAsStream("/click.wav");

            if (audioSrc == null) {
                System.err.println("🔴 ERROR CRÍTICO: No se encontró 'click.wav'. Verifica que esté directamente dentro de la carpeta 'resources'.");
                return;
            }

            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            clipClick = AudioSystem.getClip();
            clipClick.open(audioStream);
            System.out.println("✅ Audio cargado con éxito y listo para sonar.");

        } catch (UnsupportedAudioFileException e) {
            System.err.println("🔴 ERROR DE FORMATO: El archivo no es un WAV estándar. Conviértelo a WAV de 16-bit PCM.");
        } catch (Exception e) {
            System.err.println("🔴 ERROR DESCONOCIDO: " + e.getMessage());
        }
    }

    public void reproducirSonidoClick() {
        if (clipClick != null) {
            clipClick.stop();
            clipClick.setFramePosition(0);
            clipClick.start();
        }
    }
}