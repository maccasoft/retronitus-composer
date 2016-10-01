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

                        .pasm
                        .compress off

                        .section .cog_uart_driver, "ax"

                        .org     0

entry                   mov     t1,par                // get structure address
                        add     t1,#16                // skip past heads and tails

                        rdlong  t2,t1                 // get rx_pin
                        mov     rxmask,#1
                        shl     rxmask,t2

                        add     t1,#4                 // get tx_pin
                        rdlong  t2,t1
                        mov     txmask,#1
                        shl     txmask,t2

                        add     t1,#4                 // get rxtx_mode
                        rdlong  rxtxmode,t1

                        add     t1,#4                 // get bit_ticks
                        rdlong  bitticks,t1

                        add     t1,#4                 // get buffer_ptr
                        rdlong  rxbuff,t1
                        mov     txbuff,rxbuff
                        add     txbuff,#16

                        test    rxtxmode,#%100  wz    // init tx pin according to mode
                        test    rxtxmode,#%010  wc
        if_z_ne_c       or      outa,txmask
        if_z            or      dira,txmask

                        mov     txcode,#transmit      // initialize ping-pong multitasking

// Receive

receive                 jmpret  rxcode,txcode         // run a chunk of transmit code, then return

                        test    rxtxmode,#%001  wz    // wait for start bit on rx pin
                        test    rxmask,ina      wc
        if_z_eq_c       jmp     #receive

                        mov     rxbits,#9             // ready to receive byte
                        mov     rxcnt,bitticks
                        shr     rxcnt,#1
                        add     rxcnt,cnt

_bit1                   add     rxcnt,bitticks        // ready next bit period

_wait1                  jmpret  rxcode,txcode         // run a chuck of transmit code, then return

                        mov     t1,rxcnt              // check if bit receive period done
                        sub     t1,cnt
                        cmps    t1,#0           wc
        if_nc           jmp     #_wait1

                        test    rxmask,ina      wc    // receive bit on rx pin
                        rcr     rxdata,#1
                        djnz    rxbits,#_bit1

                        shr     rxdata,#32-9          // justify and trim received byte
                        and     rxdata,#$FF
                        test    rxtxmode,#%001  wz    // if rx inverted, invert byte
        if_nz           xor     rxdata,#$FF

                        rdlong  t2,par                // save received byte and inc head
                        add     t2,rxbuff
                        wrbyte  rxdata,t2
                        sub     t2,rxbuff
                        add     t2,#1
                        and     t2,#$0F
                        wrlong  t2,par

                        jmp     #receive              // byte done, receive next byte

// Transmit

transmit                jmpret  txcode,rxcode         // run a chunk of receive code, then return

                        mov     t1,par                // check for head <> tail
                        add     t1,#2 << 2
                        rdlong  t2,t1
                        add     t1,#1 << 2
                        rdlong  t3,t1
                        cmp     t2,t3           wz
        if_z            jmp     #transmit

                        add     t3,txbuff             // get byte and inc tail
                        rdbyte  txdata,t3
                        sub     t3,txbuff
                        add     t3,#1
                        and     t3,#$0F
                        wrlong  t3,t1

                        or      txdata,#$100          // ready byte to transmit
                        shl     txdata,#2
                        or      txdata,#1
                        mov     txbits,#11
                        mov     txcnt,cnt

_bit2                   test    rxtxmode,#%100  wz    // output bit on tx pin according to mode
                        test    rxtxmode,#%010  wc
        if_z_and_c      xor     txdata,#1
                        shr     txdata,#1       wc
        if_z            muxc    outa,txmask
        if_nz           muxnc   dira,txmask
                        add     txcnt,bitticks        // ready next cnt

_wait2                  jmpret  txcode,rxcode         // run a chunk of receive code, then return

                        mov     t1,txcnt              // check if bit transmit period done
                        sub     t1,cnt
                        cmps    t1,#0           wc
        if_nc           jmp     #_wait2

                        djnz    txbits,#_bit2         // another bit to transmit?

                        jmp     #transmit             // byte done, transmit next byte

// Uninitialized data

t1                      res     1
t2                      res     1
t3                      res     1

rxtxmode                res     1
bitticks                res     1

rxmask                  res     1
rxbuff                  res     1
rxdata                  res     1
rxbits                  res     1
rxcnt                   res     1
rxcode                  res     1

txmask                  res     1
txbuff                  res     1
txdata                  res     1
txbits                  res     1
txcnt                   res     1
txcode                  res     1

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
