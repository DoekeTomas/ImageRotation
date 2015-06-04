/*
 * Framework code written for the Multimedia course taught in the first year
 * of the UvA Informatica bachelor.
 *
 * Nardi Lam, 2015 (based on code by I.M.J. Kamps, S.J.R. van Schaik, R. de Vries, 2013)
 */

package nl.uva.multimedia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

import nl.uva.multimedia.audio.AudioListener;
import nl.uva.multimedia.audio.AudioPlayer;
import nl.uva.multimedia.audio.AudioVisualizationView;
import nl.uva.multimedia.audio.WaveFileAudioSource;
import nl.uva.multimedia.audio.MicrophoneAudioSource;

/*
 * An activity containing the basics for an audio processing application.
 */
public class AudioActivity extends Activity implements AudioListener {

    /*** Source constants (position in list) ***/
    public static final int SOURCE_FILE = 0;
    public static final int SOURCE_MICROPHONE = 1;

    private WaveFileAudioSource wfas;
    private MicrophoneAudioSource mas;
    private AudioPlayer audioPlayer;

    private AudioVisualizationView avv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: Dit is het "beginpunt" van de applicatie!
        // Als je vanaf hier de code stap voor stap doorloopt zul je alles tegen moeten komen.
        // De layout is gedefiniëerd in res/layout/activity_audio.xml, dit wordt ingesteld via
        // this.setContentView() hieronder.

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_audio);

        /* Create sources: */
        this.wfas = new WaveFileAudioSource(this);
        this.mas = new MicrophoneAudioSource(this);

        this.audioPlayer = new AudioPlayer();

        this.avv = (AudioVisualizationView)this.findViewById(R.id.visualization_view);

        Spinner sourceSpinner = (Spinner)this.findViewById(R.id.source_spinner);

        /* Switching between sources: */
        sourceSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case AudioActivity.SOURCE_FILE:
                                AudioActivity.this.switchToFile();
                                break;
                            case AudioActivity.SOURCE_MICROPHONE:
                                AudioActivity.this.switchToMicrophone();
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

        /* "Load file" button: */
        findViewById(R.id.load_audio_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Start an external Activity for choosing an audio file, result is returned in
                 * onActivityResult(). */
                Intent it = new Intent();
                it.setType("audio/x-wav");
                it.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(it, "Load audio file from..."),
                        SOURCE_FILE);
            }
        });

        /* Play/pause button: */
        findViewById(R.id.play_pause_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioActivity.this.wfas.toggle();

                Button b = (Button)v;
                if (AudioActivity.this.wfas.isPlaying()) {
                    b.setText(R.string.pause_icon);
                    AudioActivity.this.audioPlayer.open();
                }
                else {
                    b.setText(R.string.play_icon);
                    AudioActivity.this.audioPlayer.close();
                }
            }
        });

        /* Rewind button: */
        findViewById(R.id.rewind_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AudioActivity.this.wfas.rewind();
                    AudioActivity.this.setPaused();
                    AudioActivity.this.audioPlayer.close();
                } catch (IOException e) {
                    Toast.makeText(AudioActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        /* Record switch: */
        ((CompoundButton)findViewById(R.id.record_toggle)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            AudioActivity.this.mas.start();
                            AudioActivity.this.audioPlayer.open();
                        } else {
                            AudioActivity.this.mas.stop();
                            AudioActivity.this.audioPlayer.close();
                        }
                    }
                });

        /* Select an audio file as default source: */
        sourceSpinner.setSelection(AudioActivity.SOURCE_FILE);
    }

    private void switchToFile() {
        this.audioPlayer.close();
        this.mas.stop();
        this.wfas = new WaveFileAudioSource(this);
        this.wfas.setOnAudioListener(this);

        /* Switch out controls: */
        findViewById(R.id.file_controls).setVisibility(View.VISIBLE);
        findViewById(R.id.mic_controls).setVisibility(View.GONE);
    }

    private void setPaused() {
        ((Button)findViewById(R.id.play_pause_button)).setText(R.string.play_icon);
    }

    private void switchToMicrophone() {
        this.audioPlayer.close();
        this.wfas.pause();
        this.mas.setOnAudioListener(this);

        /* Switch out controls: */
        findViewById(R.id.file_controls).setVisibility(View.GONE);
        findViewById(R.id.mic_controls).setVisibility(View.VISIBLE);
    }

    @Override
    public void onAudio(short[] pcm, int sampleRate, int channels) {
        // TODO: Hier wordt de bronaudio doorgestuurd ter output of verdere verwerking.
        // Een goed punt om in te haken als je nog iets anders met de audio wil doen!

        /* Pass on some audio from a source to the AudioPlayer (for playback)... */
        this.audioPlayer.onAudio(pcm, sampleRate, channels);
        /* ...and to the AudioVisualizationView as well. */
        this.avv.onAudio(pcm, sampleRate, channels);
    }

    /* When an audio file has been loaded, the result gets delivered back here: */
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        super.onActivityResult(requestCode, resultCode, it);

        /* If we have an audio file... */
        if (requestCode == SOURCE_FILE && resultCode == RESULT_OK && it != null) {
            try {
                /* ...pass it to the WaveFileAudioSource: */
                this.wfas.loadFromUri(it.getData());
                this.setPaused();
                this.audioPlayer.close();
            } catch (IOException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
