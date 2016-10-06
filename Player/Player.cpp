#include <UART.h>

#define FREQUENCY       0b00
#define ENVELOPE        0b01
#define VOLUME          0b10
#define MODULATION      0b11

#define JUMP            0b0000
#define SET             0b0100
#define MODIFY          0b1000

#define wait_ms         0x10
#define STEPS           8

extern uint32_t _load_start_cog_retronitus_driver[];

extern volatile uint8_t  pause;
extern volatile uint32_t triggers[8];       // Setting triggers[X] to an "instrument/fx address", will trigger channel X with the choosen instrument/fx
extern volatile uint32_t instruments[16];   // An array of pointers to instruments
extern volatile uint32_t initPatterns[8];
extern volatile uint32_t initSteps[8];

UART Serial;

uint32_t instrument[1024];

uint32_t frequencyTable[] = {
    0x36F47DD2, // C-10: 16744.1
    0x3A390CDD, // C#10: 17739.8
    0x3DAF59A3, // D-10: 18794.6
    0x415A5A6D, // D#10: 19912.2
    0x453D319F, // E-10: 21096.3
    0x495B3042, // F-10: 22350.7
    0x4DB7DBDF, // F#10: 23679.8
    0x5256EDAC, // G-10: 25087.8
    0x573C5945, // G#10: 26579.6
    0x5C6C4CAB, // A-10: 28160.1
    0x61EB36FB, // A#10: 29834.6
    0x67BDCA8A, // B-10: 31608.7
};

uint32_t null = 0;
uint32_t muted[] = {
    SET|FREQUENCY,  0x00000000,
    SET|ENVELOPE,   0x00000000,
    SET|VOLUME,     0x00000000,
    SET|MODULATION, 0x00000000,
    JUMP,           -1 *STEPS
};

int32_t cog;
int32_t channel;

void retronitus_stop()
{
    if (cog) {
        cogstop(cog - 1);
        cog = 0;
    }
}

void retronitus_start()
{
    retronitus_stop();

    for (int i = 0; i < 16; i++) {
        instruments[i] = (uint32_t)&muted[0];
    }
    for (int i = 0; i < 8; i++) {
        triggers[i] = (uint32_t)&muted[0];
        initSteps[i] = (uint32_t)&null;
        initPatterns[i] = (uint32_t)&null;
    }

    cog = cognew(_load_start_cog_retronitus_driver, &initPatterns) + 1;
}

void retronitus_play_soundfx(int channel, uint32_t * fx)
{
    triggers[channel] = (uint32_t) fx;
}

static void retronitus_init_direct(uint32_t * musicPointer)
{
    int i;

    i = 0;
    while(*musicPointer != 0) {
        instruments[i] = *musicPointer++;
        i++;
    }
    musicPointer++;

    for (i = 0; i < 8; i++) {
        if (*musicPointer != 0) {
            initPatterns[i] = *musicPointer;
        }
        musicPointer++;
    }
}

static void retronitus_init_compact(uint8_t * musicPointer)
{
    int i, j;

    musicPointer += 2;

    // Init instrument data
    i = 0;
    while (((uint16_t *)musicPointer)[i] != 0) {
        instruments[i] = ((uint16_t *)musicPointer)[i] + (uint32_t)musicPointer;
        i++;
    }

    // Init start pattern for each channel
    i++;
    for (j = 0; j <= 7; j++) {
        if (((uint16_t *)musicPointer)[i] != 0) {
            initPatterns[j] = ((uint16_t *)musicPointer)[i] + (uint32_t)musicPointer;
        }
        else {
            initPatterns[j] = (uint32_t)&null;
        }
        i++;
    }

    // Init pattern sequencies
    i >>= 1;
    if ((((uint16_t *)musicPointer)[-1] & 0x8000) == 0) {
        while (((uint32_t *)musicPointer)[i] != 0xFFFFFFFF) {
            if (((uint32_t *)musicPointer)[i] != 0) {
                ((uint32_t *)musicPointer)[i] += (uint32_t)musicPointer;
            }
            i++;
        }
        ((uint16_t *)musicPointer)[-1] |= 0x8000;
    }
}

void retronitus_play_music(void * musicPointer)
{
    retronitus_stop();

    if ((((uint16_t *)musicPointer)[0] & 0x7FFF) == 0) {
        retronitus_init_compact((uint8_t *)musicPointer);
    }
    else {
        retronitus_init_direct((uint32_t *)musicPointer);
    }

    for (int i = 0; i < 8; i++) {
        triggers[i] = (uint32_t)&muted[0];
        initSteps[i] = (uint32_t)&null;
    }

    pause = 0;
    cog = cognew(_load_start_cog_retronitus_driver, &initPatterns) + 1;
}

void retronitus_pause_music(int p)
{
    pause = p;
}

int main() {
    int c;
    int state = 0;
    int note = 0;
    int octave = 6;

    Serial.start(31, 30, 0, 115200);
    retronitus_start();

    channel = 0;
    instrument[0] = SET|FREQUENCY;

    while(1) {
        c = Serial.read();
        
        if (c == 0x08) {
            state = 0;
        }
        else if (c == 'V') {
            c = Serial.read();
            if (c >= '1' && c <= '8') {
                channel = c - '1';
            }
            state = 0;
        }
        else if (c == 'U' || c == 'P') {
            retronitus_pause_music(1);
            for (int i = 0; i < 8; i++) {
                retronitus_play_soundfx(i, muted);
            }

            int count = Serial.read();
            count |= Serial.read() << 8;
            uint8_t * ptr = (uint8_t *)&instrument[2];
            while (count > 0) {
                *ptr = Serial.read();
                ptr++;
                count--;
            }
            if (c == 'P') {
                retronitus_play_music(&instrument[2]);
            }
            state = 0;
            continue;
        }
        else if (c == 'z') {
            instrument[1] = frequencyTable[0] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 's') {
            instrument[1] = frequencyTable[1] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 'x') {
            instrument[1] = frequencyTable[2] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 'd') {
            instrument[1] = frequencyTable[3] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 'c') {
            instrument[1] = frequencyTable[4] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 'v') {
            instrument[1] = frequencyTable[5] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 'g') {
            instrument[1] = frequencyTable[6] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 'b') {
            instrument[1] = frequencyTable[7] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 'h') {
            instrument[1] = frequencyTable[8] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 'n') {
            instrument[1] = frequencyTable[9] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 'j') {
            instrument[1] = frequencyTable[10] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        else if (c == 'm') {
            instrument[1] = frequencyTable[11] >> octave;
            retronitus_play_soundfx(channel, instrument);
            state = 0;
        }
        
        if (state == 0) {
            if (c == '0') {
                retronitus_pause_music(1);
                for (int i = 0; i < 8; i++) {
                    retronitus_play_soundfx(i, muted);
                }
            }
            else if (c >= '1' && c <= '9') {
                octave = 10 - (c - '0');
            }
            else if (c == 'C') {
                note = 0;
                state = 1;
            }
            else if (c == 'D') {
                note = 2;
                state = 1;
            }
            else if (c == 'E') {
                note = 4;
                state = 2;
            }
            else if (c == 'F') {
                note = 5;
                state = 1;
            }
            else if (c == 'G') {
                note = 7;
                state = 1;
            }
            else if (c == 'A') {
                note = 9;
                state = 1;
            }
            else if (c == 'B') {
                note = 11;
                state = 2;
            }
        }
        else if (state == 1 || state == 2) {
            if (state == 1 && c == '#') {
                note++;
            }
            state = (c == '-' || c == '#') ? 3 : 0;
        }
        else if (state == 3) {
            if (c >= '1' && c <= '9') {
                octave = 10 - (c - '0');
                instrument[1] = frequencyTable[note] >> octave;
                retronitus_play_soundfx(channel, instrument);
            }
            state = 0;
        }
    }

    return 0;
}
