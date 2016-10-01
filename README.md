## Retronitus Music Composer

Music, instruments and sound effects editor for the Retronitus sound engine.

### Music Editor

The main window allows to compose music by entering notes, instruments selection and effects in a table representing the available channels.

Each channel has four colums:

| Column | Description |
| --- | --- |
| Note | The note representation (C-3, F#4, etc.) |
| Ins. | The instrument selection (hex number) |
| Fx1, Fx2 | The effect to apply:<br/>TRx trigger sound effect x (1-6) for the selected instrument<br/>Wxx wait xx frames before next note |

Double-click or press ENTER to activate the cell editor. While the editor is active the arrow-up and arrow-down keys moves the editor up and down, the TAB keys moves the editor horizontally.

Notes can also be entered using the keyboard. The z..m keys represents the notes and the s..h keys represents the corresponding diesis notes:

```
  +---+---+   +---+---+---+  
  | s | d |   | f | g | h |  
+---+---+---+---+---+---+---+  
| z | x | c | v | b | n | m |  
+---+---+---+---+---+---+---+  
  C   D   E   F   G   A   B  
```

The octave and instruments are selected using the fields at the top-right of the window.

**Keyboard shortcuts**

| Key | Action |
| --- | --- |
| Arrows | Move the cell selection |
| Enter | Activates the cell editor and confirm the entered value |
| ESC | Exits the cell's editor |
| DEL | Blanks the cell |
| TAB | Move to the next cell while editing |
| INS | Inserts a blank entry at the current row position, shifting the channel's content down |
| SHITF+DEL | Removes the entry shifting the channel's content up |


Music can be previewer using the play icon on the top-left near the song selection drop-down. To preview music and effects it is necessary to have a Propeller chip connected to a serial port with the player firmware. The binary firmware is included and can be uploaded to the Propeller's eeprom either with your favorite uploader program or by the integrated uploader (Tools -> Upload player). The firmware expects an 80MHz setup (5MHz crystal x16) with audio channels on pins 22 and 23.

The Tools -> View compiled song menu allows to view the music data ready to be copy/pasted to an application either as a Spin DAT section or a C byte array.

### Instruments / SFX Editor

The icons at the top-right corner near the instrument selection drop-down opens the instruments editor. The sound engine doesn't make distinctions between instruments and sound effects, both uses the same command set and the only difference is that if a sound effect is used in a song the engine automatically sets the note frequency.

The command list on the left allows to enter the register values by their meaning (frequency in Hz, duration in milliseconds, amplitude levels in percentage, etc.) or by the actual hexadecimal value, do the calculations and produces a chart with the visual representation of the effects on the output.

For example, frequency can be entered either as Hz or as the note text representation C-4, F#3, etc. The buttons on the right side of the list allows to move the command up and down, add and delete entries.

The piano keyboard on the bottom allows to preview the instrument/effect when a Propeller chip with the player firmware is connected to the serial port. Like the music editor the z..m and s..h keys allows to preview the instrument, in that case the octave is the last selected with the piano keyboard.

The Get Spin Data and Get C Data buttons allows to see the actual raw values ready to be copy/pasted into a spin or C source.

### Links ###

Parallax Forum:  
http://forums.parallax.com/discussion/132826/retronitus-noise-from-the-past
