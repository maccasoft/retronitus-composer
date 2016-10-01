/*
 * Full-Duplex Serial Driver v1.2
 * Author: Chip Gracey, Jeff Martin
 * Copyright (c) 2006-2009 Parallax, Inc.
 * See end of file for terms of use.
 *
 * v1.2a - 10/6/2015 converted to C++
 * v1.2  - 5/7/2009 fixed bug in dec method causing largest negative value (-2,147,483,648) to be output as -0.
 * v1.1  - 3/1/2006 first official release.
 */

#include <propeller.h>

#include "UART.h"

#ifdef __GNUC__
#define INLINE__ static inline
#define Yield__() __asm__ volatile( "" ::: "memory" )
#else
#define INLINE__ static
static int32_t tmp__;
#define Yield__()
#endif

INLINE__ int32_t Rotl__(uint32_t a, uint32_t b) {
    return (a << b) | (a >> (32 - b));
}
INLINE__ int32_t Rotr__(uint32_t a, uint32_t b) {
    return (a >> b) | (a << (32 - b));
}
INLINE__ int32_t Lookup__(int32_t x, int32_t b, int32_t a[], int32_t n) {
    int32_t i = (x) - (b);
    return ((unsigned) i >= n) ? 0 : (a)[i];
}

extern uint8_t _load_start_cog_uart_driver[];

UART::UART() {

}

UART::UART(int32_t rxpin, int32_t txpin, int32_t mode, int32_t baudrate)
{
    rx_pin = rxpin;
    tx_pin = txpin;
    rxtx_mode = mode;
    bit_ticks = CLKFREQ / baudrate;
    buffer_ptr = (int32_t) (&rx_buffer[0]);
}

int32_t UART::start()
{
    int32_t okay = 0;

    stop();

    rx_head = 0;
    rx_tail = 0;
    tx_head = 0;
    tx_tail = 0;
    okay = (cog = cognew(_load_start_cog_uart_driver, (int32_t)&rx_head) + 1);

    return okay;
}

int32_t UART::start(int32_t rxpin, int32_t txpin, int32_t mode, int32_t baudrate)
{
    int32_t okay = 0;

    stop();

    rx_head = 0;
    rx_tail = 0;
    tx_head = 0;
    tx_tail = 0;
    rx_pin = rxpin;
    tx_pin = txpin;
    rxtx_mode = mode;
    bit_ticks = CLKFREQ / baudrate;
    buffer_ptr = (int32_t) rx_buffer;
    okay = (cog = cognew(_load_start_cog_uart_driver, (int32_t)&rx_head) + 1);

    return okay;
}

void UART::stop()
{
    if (cog) {
        cogstop(cog - 1);
        cog = 0;
    }
}

void UART::flush()
{
    while (readAvailable() >= 0) {
        Yield__();
    }
}

int32_t UART::available()
{
    return (rx_tail != rx_head);
}

int32_t UART::readAvailable()
{
    int32_t rxbyte = -1;

    if (rx_tail != rx_head) {
        rxbyte = rx_buffer[rx_tail];
        rx_tail = (rx_tail + 1) & 0xf;
    }

    return rxbyte;
}

int32_t UART::read(int32_t ms)
{
    int32_t t;
    int32_t rxbyte = 0;

    t = CNT;
    while (!(((rxbyte = readAvailable()) >= 0) || (((CNT - t) / (CLKFREQ / 1000)) > ms))) {
        Yield__();
    }

    return rxbyte;
}

int32_t UART::read()
{
    int32_t rxbyte = 0;

    while ((rxbyte = readAvailable()) < 0) {
        Yield__();
    }

    return rxbyte;
}

void UART::write(int32_t txbyte)
{
    while (!(tx_tail != ((tx_head + 1) & 0xf))) {
        Yield__();
    }
    tx_buffer[tx_head] = txbyte;
    tx_head = (tx_head + 1) & 0xf;
    if (rxtx_mode & 0x8) {
        read();
    }
}

void UART::writeBytes(const uint8_t * txbytes, uint32_t length)
{
    while(length > 0) {
        write(*txbytes++);
        length--;
    }
}

/*
 * TERMS OF USE: MIT License
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
