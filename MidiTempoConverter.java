/*
 * Title: Simple Midi Tempo Converter, By Dan H 2016
 *
 * Description : Changes the underlying master tempo of a midi file and
 * time shifts all the midi events in line with that new tempo so that
 * the actual playback of the tracks are unaffected. This is useful when
 * importing midi files into projects that differ from the tempo of the
 * imported midi file.
 *
 * Important:
 * - For simplicity the original MIDI file is overwritten with the new one
 * - Only the main tempo (MetaEvent) is changed. This typically affects the whole
 *   file and all channels and tracks within that file assuming you don't
 *   have any further tempo change MIDI events. Subsequent tempo change events
 *   remain unchanged.
 * 
 * Credits:
 * Parts of this code are derived from Sami Koivu's post on Stackoverflow
 * http://stackoverflow.com/questions/3850688/reading-midi-files-in-java
 *
 */

import java.io.File;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Track;

/**
 *
 * MidiTempoConvert Class
 * 
 * @author Dan H
 */
public class MidiTempoConverter {
    
    public static File file;                // Midi file from arg[0]
    public static int tempo;                // New tempo from arg[1]
    public static boolean debug = false;    // Debug output enabled
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    /**
     * Main Application
     *
     * @author Dan H
     */
    public static void main(String[] args) throws Exception {

        initApp(args);

        try {

            Sequence sequence = MidiSystem.getSequence(file);

            // Use the sequencer interface to extract the incumbent tempo of the MIDI file
            Sequencer sequencer = MidiSystem.getSequencer(false);
            sequencer.setSequence(sequence);
            int oldTempo = (int) sequencer.getTempoInBPM();
            System.out.println("Old Tempo: " + oldTempo);
            System.out.println("New Tempo: " + tempo);
            System.out.println("Updating MIDI Events");

            int trackNumber = 0;
            boolean masterTempoChanged = false;     //We only change the first MetaEvent (this stops subsequent tempo changes)

            for (Track track : sequence.getTracks()) {
                trackNumber++;
                if (debug) {
                    System.out.println("Found Track " + trackNumber + ": size = " + track.size());
                }
                for (int i = 0; i < track.size(); i++) {

                    MidiEvent event = track.get(i);
                    MidiMessage message = event.getMessage();
                    if (debug) {
                        debugMidiEvents(event);
                    }

                    //Set new Tick (scale existing tick by the new Tempo)
                    long newTick = (long) ((double) event.getTick() / oldTempo * tempo);
                    event.setTick(newTick);

                    //Change the first/master MetaEvent instance of Tempo
                    if (message instanceof MetaMessage) {
                        MetaMessage mm = (MetaMessage) message;
                        if ((mm.getType() == 0x51) && (masterTempoChanged == false)) {
                            mm.setMessage(0x51, convertTempo(tempo), 3);
                            System.out.println("Found Master Tempo (Set = " + tempo + ")");
                            masterTempoChanged = true;
                        }
                    }
                }
            }

            System.out.println("Saving Changes to the MIDI File");
            MidiSystem.write(sequence, 1, file);
            System.out.println("Done!");
            System.out.println();

        } catch (Exception e) {
            System.out.println("Error: Can't read MIDI File. " + e.getMessage());
        }

    }
    
    /**
     * The tempo is set in a Meta Event (type 0x51, decimal 81). Meta
     * events are used for special non-MIDI events. They are like a form of
     * informational attribute typically at the top of a MIDI file. The new
     * tempo value has to be given in microseconds per quarter (MPQ). You can
     * calculate MPQ from BPM: mpq = 60000000 / bpm.
     *
     * @author Dan H
     */
    public static byte[] convertTempo(int tempo) {
    
        byte[] result = new byte[3]; //The tempo value is a 3 bytes BIG_ENDIAN
        
        tempo = 60000000 / tempo;
        result[0] = (byte)((tempo >> 16) & 0xFF);
        result[1] = (byte)((tempo >> 8) & 0xFF);
        result[2] = (byte)(tempo & 0xFF);
        
        return result;
    }
    
    /**
     * Initialise some key fields for the application
     * @author Dan H
     */
    public static void initApp(String[] args) throws Exception {

        System.out.println("\nMidi Tempo Converter 1.0");
        System.out.println("==========================");
        
        if (args.length >= 2) {
            file = new File(args[0]);
            if (!file.exists() || file.isDirectory()) {
                System.out.println("Error: Midi file does not exist !");
                System.exit(0);
            }
            try {
                tempo = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("Error: Tempo is not a valid integer !");
                System.exit(0);
            }
            if (args.length == 3 && args[2].equals("debug")) {
                debug = true;
            }
        }
        else {
                System.out.println("Usage: midiconverttempo file_name tempo [debug]");
                System.exit(0);
        }
    }
    
    /**
     * Essentially this code interprets the actual midi events and be used
     * to see what exactly is inside the file for debugging
     * 
     * Code derived from Sami Koivu post on stackoverflow
     * http://stackoverflow.com/questions/3850688/reading-midi-files-in-java
     * 
     * @author Dan H
     */
    public static void debugMidiEvents(MidiEvent event) {
        
        System.out.print("@" + event.getTick() + " ");
        MidiMessage message = event.getMessage();
        
        if (message instanceof MetaMessage) {
            MetaMessage mm = (MetaMessage) message;
            System.out.println("MetaMessage Type: " + mm.getType());
        }

        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            System.out.print("Channel: " + sm.getChannel() + " ");
            if (sm.getCommand() == sm.NOTE_ON) {
                int key = sm.getData1();
                int octave = (key / 12) - 1;
                int note = key % 12;
                String noteName = NOTE_NAMES[note];
                int velocity = sm.getData2();
                System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
            } else if (sm.getCommand() == sm.NOTE_OFF) {
                int key = sm.getData1();
                int octave = (key / 12) - 1;
                int note = key % 12;
                String noteName = NOTE_NAMES[note];
                int velocity = sm.getData2();
                System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
            } else {
                System.out.println("Command:" + sm.getCommand());
            }
        } else {
            System.out.println("Other message: " + message.getClass());
        }

    }
}
