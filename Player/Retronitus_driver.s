/*
 * Retronitus - v0.10
 * Copyright (c) 2011-07 Johannes Ahlebrand
 *
 * Modifications for P8X Game System
 * Copyright (c) 2015-2016 Marco Maccaferri
 *
 * TERMS OF USE: Parallax Object Exchange License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

                        .pasm
                        .compress off

                        .section .cog_retronitus_driver, "ax"

                        .equ    LPIN, 23
                        .equ    RPIN, 22
                        .equ    SAMPLE_RATE, 78000                  // Hz

// ───────────────────────────────────────────────────────────
//                      Initialization
// ───────────────────────────────────────────────────────────
waitCounter             mov     ctra, tempValue1                    //  These three line gets reused as variables
outL                    mov     ctrb, tempValue2                    //  (This driver REALLY pushes the 512 instruction limit)
outR                    mov     dira, tempValue3                    //
// -----------------------------------------------------------
reStart                 mov     tempValue1, par
                        movd    :par, #c1_patternPointer
                        mov     tempValue2, #16
:par                    rdlong  0-0, tempValue1
                        add     :par, val512
                        add     tempValue1, #4
                        djnz    tempValue2, #:par
// -----------------------------------------------------------
                        mov     waitCounter, cnt
                        add     waitCounter, sampleRate

// ───────────────────────────────────────────────────────────
//                      Task scheduler
// ───────────────────────────────────────────────────────────
mainLoop                cmpsub  tablePointerOffset, #1           wc //
            if_nc       mov     tablePointerOffset, #10             //  currentTableP = #waveTablePointer1 + tablePointerOffset
                        cmp     tablePointerOffset, #8           wz
            if_z        jmp     #trigger                            //  On frame 8/10 , jump to the trigger subroutine
                        cmp     tablePointerOffset, #9       wz, wc
enableM     if_z        jmp     #music                              //  On frame 9/10 , jump to the music subroutine
        if_nz_and_nc    jmp     #prepMusic                          //  On frame 10/10, jump to the prepare music subroutine

// ───────────────────────────────────────────────────────────
// Read and interpret instructions/commands from hub memory         //  On frame 0 to 7, handle instruction execution for channel 0 - 7
// ───────────────────────────────────────────────────────────
                        mov     tempValue3, #waveTablePointer1
                        add     tempValue3, tablePointerOffset
                        movs    readCommand, tempValue3             //  Read instruction from HUB memory for channel X
                        movd    incCurrTableP, tempValue3           //
readCommand             rdlong  tempValue2, 0-0
                        mov     tempValue1, tempValue2
// -----------------------------------------------------------
incCurrTableP           add     0-0, #4                             //  If not in repeat mode, point to next long (data)
                        movd    incCurrTblP2, tempValue3
// -----------------------------------------------------------
                        mov     tempValue4, #c1_freq
                        add     tempValue4, tablePointerOffset      //  currentRegPointer = ch1_freq + regOffsetPointer + registerSelect
                        and     tempValue2, #3
                        shl     tempValue2, #3
                        add     tempValue4, tempValue2
// -----------------------------------------------------------
                        movs    setHubAddress,  tempValue3          //
                        movd    jump, tempValue3                    //  Read in data from the hub address contained in "currentTableP"
// -----------------------------------------------------------
                        cmp     channelUpdate, #0                wz //
            if_nz       jmp     #setHubAddress                      //
                        rdbyte  tempValue2, pausePointer         wz //  Handle pausing
            if_nz       movs    enableM, #phaseAcc                  //
            if_z        movs    enableM, #music                     //
// -----------------------------------------------------------
setHubAddress           rdlong  tempValue2, 0-0                     //
// -----------------------------------------------------------
                        test    tempValue1, #(3<<2)              wz //
jump        if_z        add     0-0, tempValue2                     //  If command = 0: currentTableP += dataValue
            if_z        jmp     #incCurrTblP2                       //
                        movd    set, tempValue4                     //  Prepare destination registers
                        movd    modify, tempValue4                  //
                        test    tempValue1, #(2<<2)              wz //
set         if_z        mov     0-0, tempValue2                     //  If command = 1: currentRegPointer  = dataValue
modify      if_nz       add     0-0, tempValue2                     //  If command = 2: currentRegPointer += dataValue
// -----------------------------------------------------------
                        andn    tempValue1, #15                  wz //
incCurrTblP2            sumnz   0-0, #4                             //  If not in repeat mode, point to next long (command)

// ───────────────────────────────────────────────────────────
//                      Sound synthesis
// ───────────────────────────────────────────────────────────
phaseAcc                add     c1_phaseAccumulator, c1_freq        //  Increment phase accumulators
                        add     c2_phaseAccumulator, c2_freq
                        add     c3_phaseAccumulator, c3_freq
                        add     c4_phaseAccumulator, c4_freq
                        add     c5_phaseAccumulator, c5_freq
                        add     c6_phaseAccumulator, c6_freq
                        add     c7_phaseAccumulator, c7_freq
                        add     c8_phaseAccumulator, c8_freq
// -----------------------------------------------------------   Handle modulation
                        test    c1_mod, #511                     wz //  Test if any of the 9 LSBs are set
            if_nz       movi    c1_modulation, c1_mod               //  If true, set modulation value to "the 9 LSBs << 23"
            if_z        add     c1_modulation, c1_mod               //  Else, Modulate
                        test    c2_mod, #511                     wz
            if_nz       movi    c2_modulation, c2_mod
            if_z        add     c2_modulation, c2_mod
                        test    c3_mod, #511                     wz
            if_nz       movi    c3_modulation, c3_mod
            if_z        add     c3_modulation, c3_mod
                        test    c4_mod, #511                     wz
            if_nz       movi    c4_modulation, c4_mod
            if_z        add     c4_modulation, c4_mod
                        test    c5_mod, #511                     wz
            if_nz       movi    c5_modulation, c5_mod
            if_z        add     c5_modulation, c5_mod
                        test    c6_mod, #511                     wz
            if_nz       movi    c6_modulation, c6_mod
            if_z        add     c6_modulation, c6_mod
// -----------------------------------------------------------   Handle envelopes
                        add     c1_volume, c1_ASD                   //  Inc/Dec volume with the rate of ch1_ASD
                        test    c1_volume, volumeBits            wz //  Test if max/min value is reached
            if_z        movi    c1_volume, c1_ASD                   //  If true, lock value to: "the 9 LSBs of ch1_ASD" << 23
                        max     c1_volume, c1_vol                   //  Sets the sustain level
                        add     c2_volume, c2_ASD
                        test    c2_volume, volumeBits            wz
            if_z        movi    c2_volume, c2_ASD
                        max     c2_volume, c2_vol
                        add     c3_volume, c3_ASD
                        test    c3_volume, volumeBits            wz
            if_z        movi    c3_volume, c3_ASD
                        max     c3_volume, c3_vol
                        add     c4_volume, c4_ASD
                        test    c4_volume, volumeBits            wz
            if_z        movi    c4_volume, c4_ASD
                        max     c4_volume, c4_vol
                        add     c5_volume, c5_ASD
                        test    c5_volume, volumeBits            wz
            if_z        movi    c5_volume, c5_ASD
                        max     c5_volume, c5_vol
                        add     c6_volume, c6_ASD
                        test    c6_volume, volumeBits            wz
            if_z        movi    c6_volume, c6_ASD
                        max     c6_volume, c6_vol
                        add     c7_volume, c7_ASD
                        test    c7_volume, volumeBits            wz
            if_z        movi    c7_volume, c7_ASD
                        max     c7_volume, c7_vol
                        add     c8_volume, c8_ASD
                        test    c8_volume, volumeBits            wz
            if_z        movi    c8_volume, c8_ASD
                        max     c8_volume, c8_vol
// -----------------------------------------------------------   Waveform shaping
ch1_Square1             cmp     c1_modulation, c1_phaseAccumulator wc
                        mov     tempValue1, c1_volume
                        shr     tempValue1, #3
                        sumc    outL, tempValue1
ch1_Square2             cmp     c2_modulation, c2_phaseAccumulator wc
                        mov     tempValue1, c2_volume
                        shr     tempValue1, #3
                        sumc    outL, tempValue1
ch1_Square3             cmp     c3_modulation, c3_phaseAccumulator wc
                        mov     tempValue1, c3_volume
                        shr     tempValue1, #3
                        sumc    outL, tempValue1
ch4_Saw                 mov     tempValue1, c4_phaseAccumulator    wc
                        abs     tempValue2, c4_modulation
                        sumc    tempValue1, tempValue2             wc
                        mov     tempValue2, c4_volume
                        call    #sar8Multiply
                        add     outL, tempValue3
ch5_Saw                 mov     tempValue1, c5_phaseAccumulator    wc
                        abs     tempValue2, c5_modulation
                        sumc    tempValue1, tempValue2             wc
                        mov     tempValue2, c5_volume
                        call    #sar8Multiply
                        add     outL, tempValue3
ch6_Saw                 mov     tempValue1, c6_phaseAccumulator    wc
                        abs     tempValue2, c6_modulation
                        sumc    tempValue1, tempValue2             wc
                        mov     tempValue2, c6_volume
                        call    #sar8Multiply
                        add     outL, tempValue3
ch7_Triangle            abs     tempValue1, c7_phaseAccumulator
                        and     tempValue1, c7_mod
                        sub     tempValue1, val30bit
                        sar     tempValue1, #7
                        mov     tempValue2, c7_volume
                        call    #multiply
                        add     outL, tempValue3
ch8_Noise               cmp     c8_phaseAccumulator, c8_freq       wc
            if_c        ror     noiseValue, #15
            if_c        add     noiseValue, c8_mod
                        mov     tempValue1, noiseValue
                        mov     tempValue2, c8_volume
                        call    #sar8Multiply
                        add     outL, tempValue3
// -----------------------------------------------------------   Mix channels
mixer                   waitcnt waitCounter, sampleRate
                        mov     FRQA, outL
                        mov     FRQB, outL
                        mov     outL, val31bit
                        mov     outL, val31bit
                        jmp     #mainLoop

// ───────────────────────────────────────────────────────────
//                Handle sound triggering
// ───────────────────────────────────────────────────────────
trigger                 cmpsub  triggerOffset, #1                wc //
            if_nc       mov     triggerOffset, #7                   //
                        mov     selectedTrigPointer, triggerOffset  //  tempValue = iterates through the hub addresses of trigger1 - trigger8
                        shl     selectedTrigPointer, #2             //
                        add     selectedTrigPointer, triggerPointer //
                        mov     tempValue3, #waveTablePointer1      //
                        rdlong  tempValue2, selectedTrigPointer  wz //
// -----------------------------------------------------------
                        add     tempValue3, triggerOffset           //  Sets the tablePointer to be affected
                        movd    wrToWavT, tempValue3                //
            if_nz       wrlong  zero, selectedTrigPointer           //
wrToWavT    if_nz       mov     0-0, tempValue2                     //  set the address in the selected waveTablePointer
// ───────────────────────────────────────────────────────────
//                Instruction repeat handling
// ───────────────────────────────────────────────────────────
                        movs    readRepAmount, tempValue3           //
                        movd    incCT, tempValue3                   //  Reads in the repetation amount value
// -----------------------------------------------------------
readRepAmount           rdlong  tempValue3, 0-0
                        shr     tempValue3, #4
// -----------------------------------------------------------
                        mov     tempValue1, #repeatCounter1         //
                        add     tempValue1, triggerOffset           //  Sets which repeatCounter to affect
                        movd    src1, tempValue1
                        movd    src2, tempValue1
                        movd    src3, tempValue1
// -----------------------------------------------------------
src1        if_nz       mov     0-0, #0
src2                    cmpsub  0-0, #1                      wc, wz //  Decrement the selected repeatCounter and check if zero(nc) or one(z)
src3        if_nc       mov     0-0, tempValue3                     //  If zero, set the the selected repeatCounter to command
incCT       if_z        add     0-0, #8                             //  If one, increment the selected waveTablePointer
                        jmp     #phaseAcc

// ───────────────────────────────────────────────────────────
//  Point variables in the music routine to the next channel (round robin)
// ───────────────────────────────────────────────────────────
prepMusic               mov     tempValue1, #c1_freq                //  Freq
                        add     tempValue1, triggerOffset
                        movd    setNote, tempValue1
                        movd    F3, tempValue1
                        sub     tempValue1, #51                     //  stepWait
                        movd    A1, tempValue1
                        movd    A2, tempValue1
                        movd    A3, tempValue1
                        add     tempValue1, #83                     //  noteOn
                        movd    C1, tempValue1
                        movd    C2, tempValue1
                        movs    C3, tempValue1
                        add     tempValue1, #16                     //  note
                        movd    E1, tempValue1
                        movd    E2, tempValue1
                        movd    E3, tempValue1
                        movd    E4, tempValue1
                        movs    E5, tempValue1
                        movd    E6, tempValue1
                        movd    E7, tempValue1
                        movd    E8, tempValue1
                        add     tempValue1, #8                      //  octave
                        movd    F1, tempValue1
                        movd    F2, tempValue1
                        movs    F3, tempValue1
                        movd    F4, tempValue1
                        movd    F5, tempValue1
                        add     tempValue1, #8                      //  patternPointer
                        movs    G1, tempValue1
                        movd    G2, tempValue1
                        add     tempValue1, #8                      //  stepPointer
                        movs    pattern, tempValue1
                        movd    B1, tempValue1
                        movs    B3, tempValue1
                        movd    B4, tempValue1
                        jmp     #phaseAcc

// ───────────────────────────────────────────────────────────
//                      Music routine
// ───────────────────────────────────────────────────────────
music                   add     tempoAccumulator, tempo          wc
            if_c        mov     channelUpdate, #255
                        shr     channelUpdate, #1                wc
            if_nc       jmp     #patternHandler
// -----------------------------------------------------------
A1                      cmpsub  c1_stepWait, #1                  wc
            if_c        jmp     #phaseAcc
// -----------------------------------------------------------  Handle pattern stepping and note triggering
pattern                 rdbyte  tempValue1, c1_stepPointer       wz //
            if_z        jmp     #phaseAcc
B1                      add     c1_stepPointer, #1                  //  If note equals zero, don// t increment the step pointer
A2                      mov     c1_stepWait, tempValue1             //
A3                      and     c1_stepWait, #7                     //  Gets "note shift" and "step wait"
// -----------------------------------------------------------
                        shr     tempValue1, #3                      //
                        cmpsub  tempValue1, #25              wz, wc //
            if_c        shl     tempValue1, #3                      //
C3          if_c        add     tempValue1, c1_noteOn               //
        if_c_and_nz     wrlong  tempValue1, selectedTrigPointer     //
            if_c        jmp     #phaseAcc                           //
C1          if_nc       wrlong  c1_noteOn, selectedTrigPointer      //                            //
// -----------------------------------------------------------
                        sub     tempValue1, #12                     //  Note -> -12 - +12
E1                      add     c1_note, tempValue1              wc
E2                      cmps    c1_note, #0                      wc //  If currentNote < 0
E3          if_c        add     c1_note, #12                        //    currentNote += 12
F1          if_c        add     c1_octave, #1                       //    octave += 1
E4                      cmpsub  c1_note, #12                     wc //  If currentNote > 11 | currentNote -= 12
// -----------------------------------------------------------
                        mov     tempValue1, #fTable
E5                      add     tempValue1, c1_note
                        movs    setNote, tempValue1                 //  Sets the current frequency
// -----------------------------------------------------------
F2          if_c        sub     c1_octave, #1                       //    octave -= 1
// -----------------------------------------------------------                        //
setNote                 mov     c1_freq, 0-0
F3                      shr     c1_freq, c1_octave
                        jmp     #phaseAcc
// ───────────────────────────────────────────────────────────
//               Handle loading of new patterns
// ───────────────────────────────────────────────────────────
patternHandler
B3                      rdbyte  tempValue1, c1_stepPointer       wz
            if_nz       jmp     #phaseAcc
                        // nop
G1                      rdlong  tempValue1, c1_patternPointer    wz
            if_z        jmp     #phaseAcc                           //  <- Halts further pattern incrementaion when a NULL-pattern is detected
                        cmp     tempValue1, patternEnd           wz
            if_z        jmp     #reStart                            //  <- Restarts the entire tune when a "special" pattern is detected
                        test    tempValue1, val15bit             wz
                        andn    tempValue1, val15bit
B4                      mov     c1_stepPointer, tempValue1
G2                      add     c1_patternPointer, #4
// -----------------------------------------------------------
F4          if_z        mov     c1_octave, tempValue1
F5          if_z        shr     c1_octave, #28
E6          if_z        mov     c1_note, tempValue1                 //  This takes care of "octave" and "note" init values
E7          if_z        shr     c1_note, #24
E8          if_z        and     c1_note, #15
// -----------------------------------------------------------
            if_nz       mov     tempo, tempValue1
            if_nz       shr     tempo, #4                           //  If tempo bit is set, set tempo
            if_nz       and     tempo, tempoBits
// -----------------------------------------------------------
                        shr     tempValue1, #14
                        and     tempValue1, #63                     //  Note = noteOn address
                        add     tempValue1, instrumenPointer        //
C2                      rdlong  c1_noteOn, tempValue1
                        jmp     #phaseAcc

// ───────────────────────────────────────────────────────────
//     Multiplication     r1(I32) = arg1(I32) * arg2(I6)
// ───────────────────────────────────────────────────────────
sar8Multiply            sar     tempValue1, #9
multiply                mov     tempValue3, #0
                        test    tempValue2, val26bit             wc
            if_c        add     tempValue3, tempValue1
                        shl     tempValue1, #1
                        test    tempValue2, val27bit             wc
            if_c        add     tempValue3, tempValue1
                        shl     tempValue1, #1
                        test    tempValue2, val28bit             wc
            if_c        add     tempValue3, tempValue1
                        shl     tempValue1, #1
                        test    tempValue2, val29bit             wc
            if_c        add     tempValue3, tempValue1
                        shl     tempValue1, #1
                        test    tempValue2, val30bit             wc
            if_c        add     tempValue3, tempValue1
                        shl     tempValue1, #1
                        test    tempValue2, val31bit             wc
            if_c        add     tempValue3, tempValue1
sar8Multiply_ret
multiply_ret  ret

// ───────────────────────────────────────────────────────────
//                     Frequency table
// ───────────────────────────────────────────────────────────
fTable                  long    $36F47DD2   // C-10: 16744.1
                        long    $3A390CDD   // C#10: 17739.8
                        long    $3DAF59A3   // D-10: 18794.6
                        long    $415A5A6D   // D#10: 19912.2
                        long    $453D319F   // E-10: 21096.3
                        long    $495B3042   // F-10: 22350.7
                        long    $4DB7DBDF   // F#10: 23679.8
                        long    $5256EDAC   // G-10: 25087.8
                        long    $573C5945   // G#10: 26579.6
                        long    $5C6C4CAB   // A-10: 28160.1
                        long    $61EB36FB   // A#10: 29834.6
                        long    $67BDCA8A   // B-10: 31608.7

// ───────────────────────────────────────────────────────────
//     Pre-initialized masks and reference values
// ───────────────────────────────────────────────────────────
val31bit                long    $80000000
val30bit                long    $40000000
val29bit                long    $20000000
val28bit                long    $10000000
val27bit                long    $8000000
val26bit                long    $4000000
val15bit                long    $8000
tempoBits               long    $ffff0000
volumeBits              long    $fc000000
patternEnd              long    $ffffffff

// ───────────────────────────────────────────────────────────
//                   Variables and pointers
// ───────────────────────────────────────────────────────────
pausePointer            long    @_pause
triggerPointer          long    @_triggers     // This points to the first trigger register in hub memory
instrumenPointer        long    @_instruments  // This points to the first instrument position in hub memory
channelUpdate           long    0
zero                    long    0
tablePointerOffset      long    1
triggerOffset           long    1
val512                  long    1 << 9
tempo                   long    150 << 16
sampleRate              long    80000000 / SAMPLE_RATE                  // <- These
tempValue1              long    $18000000 | LPIN                        // <- four
tempValue2              long    $18000000 | RPIN                        // <- variables
tempValue3              long    ((1 << LPIN) | (1 << RPIN)) & $FFFFFFFE // <- are used to initialize retronitus
c1_stepWait             long    1
c2_stepWait             long    1
c3_stepWait             long    1
c4_stepWait             long    1
c5_stepWait             long    1
c6_stepWait             long    1
c7_stepWait             long    1
c8_stepWait             long    1

tempValue4              res     1
noiseValue              res     1
selectedTrigPointer     res     1
tempoAccumulator        res     1
patternType             res     1
c1_phaseAccumulator     res     1
c2_phaseAccumulator     res     1
c3_phaseAccumulator     res     1
c4_phaseAccumulator     res     1
c5_phaseAccumulator     res     1
c6_phaseAccumulator     res     1
c7_phaseAccumulator     res     1
c8_phaseAccumulator     res     1
c1_volume               res     1
c2_volume               res     1
c3_volume               res     1
c4_volume               res     1
c5_volume               res     1
c6_volume               res     1
c7_volume               res     1
c8_volume               res     1
c1_modulation           res     1
c2_modulation           res     1
c3_modulation           res     1
c4_modulation           res     1
c5_modulation           res     1
c6_modulation           res     1
repeatCounter1          res     1
repeatCounter2          res     1
repeatCounter3          res     1
repeatCounter4          res     1
repeatCounter5          res     1
repeatCounter6          res     1
repeatCounter7          res     1
repeatCounter8          res     1
waveTablePointer1       res     1
waveTablePointer2       res     1
waveTablePointer3       res     1
waveTablePointer4       res     1
waveTablePointer5       res     1
waveTablePointer6       res     1
waveTablePointer7       res     1
waveTablePointer8       res     1

// ───────────────────────────────────────────────────────────
//                Internal Retronitus registers
// ───────────────────────────────────────────────────────────
c1_freq                 res     1  //  bit 31-0:  Signed frequency
c2_freq                 res     1
c3_freq                 res     1
c4_freq                 res     1
c5_freq                 res     1
c6_freq                 res     1
c7_freq                 res     1
c8_freq                 res     1
c1_ASD                  res     1  //  bit 31-5:  Attack/Decay/AM rate, bit 4-0:  Amplitude modulation amount
c2_ASD                  res     1
c3_ASD                  res     1
c4_ASD                  res     1
c5_ASD                  res     1
c6_ASD                  res     1
c7_ASD                  res     1
c8_ASD                  res     1
c1_vol                  res     1  //  bit 31-27: Sustain level
c2_vol                  res     1
c3_vol                  res     1
c4_vol                  res     1
c5_vol                  res     1
c6_vol                  res     1
c7_vol                  res     1
c8_vol                  res     1
c1_mod                  res     1  //  bit 31-10: Waveform modulation rate, bit 9-0: Set fixed wafeform
c2_mod                  res     1
c3_mod                  res     1
c4_mod                  res     1
c5_mod                  res     1
c6_mod                  res     1
c7_mod                  res     1
c8_mod                  res     1
c1_noteOn               res     8  //  Address to the noteOn instrument
c1_noteOff              res     8  //  Address to the noteOff instrument"
c1_note                 res     8  //  Holds the current note (0 - 11)
c1_octave               res     8  //  Holds the current octave
c1_patternPointer       res     8  //  Address to the current pattern init
c1_stepPointer          res     8  //  Address to the current pattern step


                        .data

                        .global _pause, _triggers, _instruments, _initPatterns, _initSteps

_pause                  .res    1
_triggers               .res    8  // Setting triggers[X] to an "instrument/fx address", will trigger channel X with the choosen instrument/fx
_instruments            .res    16 // An array of pointers to instruments
_initPatterns           .res    8
_initSteps              .res    8

                        fit     $1F0

