#ifndef UART_H_
#define UART_H_

#include <propeller.h>
#include <stdint.h>

class UART {
public:
    UART();
    UART(int32_t rxpin, int32_t txpin, int32_t mode, int32_t baudrate);

    int32_t start();
    int32_t start(int32_t rxpin, int32_t txpin, int32_t mode, int32_t baudrate);
    void    stop();

    void    flush();
    int32_t available();

    int32_t read();
    int32_t read(int32_t ms);
    int32_t readAvailable();

    void    write(int32_t txbyte);
    void    writeBytes(const uint8_t * txbytes, uint32_t length);

private:
    // cog flag/id
    volatile int32_t cog;

    // 9 contiguous longs
    volatile int32_t rx_head;
    volatile int32_t rx_tail;
    volatile int32_t tx_head;
    volatile int32_t tx_tail;
    volatile int32_t rx_pin;
    volatile int32_t tx_pin;
    volatile int32_t rxtx_mode;
    volatile int32_t bit_ticks;
    volatile int32_t buffer_ptr;

    // transmit and receive buffers
    volatile uint8_t rx_buffer[16];
    volatile uint8_t tx_buffer[16];
};

#endif /* UART_H_ */
