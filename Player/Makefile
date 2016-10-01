#TOOLCHAIN := /opt/parallax/bin/

# basic gnu tools
CC := $(TOOLCHAIN)propeller-elf-gcc
CXX := $(TOOLCHAIN)propeller-elf-g++
LD := $(TOOLCHAIN)propeller-elf-ld
AS := $(TOOLCHAIN)propeller-elf-as
AR := $(TOOLCHAIN)propeller-elf-ar
OBJCOPY := $(TOOLCHAIN)propeller-elf-objcopy
LOADER := $(TOOLCHAIN)propeller-load

NAME := Player
MODEL := lmm

CFLAGS := -Os -Wall -fdata-sections -ffunction-sections -I.
CXXFLAGS := -Os -Wall -fdata-sections -ffunction-sections -I.
SPINFLAGS := -q -Ox
LDFLAGS := -s -Wl,--gc-sections

OBJS := \
	Player.o \
	Retronitus_driver.o \
    UART.o \
    UART_driver.o

LIBS :=

all: $(NAME).elf $(NAME).binary


$(NAME).elf: $(OBJS) Makefile
	$(CC) -m$(MODEL) $(LDFLAGS) -o $@ $(OBJS) $(LIBS)


#
# default rules
#
%.o: %.c
	$(CC) -m$(MODEL) $(CFLAGS) -o $@ -c $<

%.o: %.cpp
	$(CXX) -m$(MODEL) $(CXXFLAGS) -o $@ -c $<

%.o: %.s
	$(CC) -x assembler-with-cpp -o $@ -c $<

%.binary: %.elf
	$(LOADER) -s $<


#
# cleanup
#
clean:
	rm -f *.o *.d *.elf *.a *.dat *.cog *.ecog *.binary


#
# upload and run
#
run: $(NAME).elf
	$(LOADER) $(BOARDFLAG) $(NAME).elf -r -p $(PORT)


#
# upload, write to eeprom and run
#
burn: $(NAME).elf
	$(LOADER) $(BOARDFLAG) $(NAME).elf -r -e -p $(PORT)
