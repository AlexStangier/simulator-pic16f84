package model;


import controller.Operation;

public class Register {

    //Initialization of RAM storage FILE REGISTER
    static int[] ram_Bank0 = new int[128];

    static int[] ram_Bank1 = new int[128];


    //Initialization of the Programm Counter
    static int programm_Counter = 0;                                                              //Program Counter Latch Low     PCL

    //Initialization of the Working Register
    static int working_Register = 0;

    //Initialization of the Status Register                                                       //Contains all Status flags: 0:C 1:DC 2:Z 3:PD 4:TO 5:RP0 6:RP1 7:IRP
    static int status_Register = 0;

    ///Initialization of the File Select Register Bank 0                                          //contains pointer for indirect addressing
    static int file_Save_Register_Bank0 = 0;

    ///Initialization of the File Select Register Bank 1                                          //contains pointer for indirect addressing
    static int file_Save_Register_Bank1 = 0;

    //Initialization of the Stack Pointer
    static byte stackpointer = 0;

    //Initialization of the Stack Register
    static int[] stack_Register = new int[8];

    //Initialization of the Timerregister TMR0
    static int tmr0 = 0;

    //Initialization of INTCON Register
    static int[] intcon = new int[8];                                                             //[0]RB Port Change [1]RBO interrupt [2]TMR0 overflow [3]RBIE [4]INTE [5]T0IE [6]EEIE [7]GIE


    /**
     * Sets all Registers to null
     */
    public void resetRegisters() {
        stackpointer = 0;
        programm_Counter = 0;

        file_Save_Register_Bank0 = 0;
        file_Save_Register_Bank1 = 0;
        working_Register = 0;

        intcon[7] = 0;
        intcon[5] = 0;
        intcon[4] = 0;
        intcon[3] = 0;
        intcon[2] = 0;
        intcon[1] = 0;
        intcon[0] = 0;

        tmr0 = 0;


        for (int i = 0; i < stack_Register.length; i++) {
            stack_Register[i] = 0;
        }
        for (int i = 0; i < ram_Bank0.length; i++) {
            ram_Bank0[i] = 0;
            ram_Bank1[i] = 0;
        }
    }

    /**
     * Stackoperations
     **/

    public static void incrementStackPointer() {

        if (stackpointer == 7) {
            stackpointer = 0;
        } else if (stackpointer == -1) {
            stackpointer = 0;
        } else {
            stackpointer++;
        }
    }


    public void decrementStackPointer() {
        if (stackpointer == 0) {
            stackpointer = 7;
        } else if (stackpointer == -1) {
            stackpointer = 0;
        } else {
            stackpointer--;

        }

    }


    /**
     * PCL Operations
     **/

    public void setProgramm_Counter(int counter) {
        programm_Counter = counter;
    }

    public void incrementProgrammCounter() {
        programm_Counter++;
    }

    /**
     * Returns the current value of the Program Counter
     *
     * @return an int that represents the current position of the Program Counter
     */
    public int getProgramm_Counter() {
        return programm_Counter;
    }

    /**
     * Working register operations
     **/

    public void setWorking_Register(int working_Register) {

        Register.working_Register = working_Register % 256;
    }


    public int getWorking_Register() {
        return working_Register;

    }

    public void setStack_Register(Register reg, int pcl) {
        setStack(reg, pcl);
    }

    public int getFromStack_Register(int index) {
        return stack_Register[index];
    }

    public static void setStack(Register reg, int pcl) {
        stack_Register[stackpointer] = pcl;
    }


    /**
     * STACK Operations
     **/

    public void push(int pcl) {
        stack_Register[stackpointer] = pcl;
        incrementStackPointer();
    }

    public int pop() {
        int i = stack_Register[getStackpointer()];
        decrementStackPointer();
        return i;
    }


    public int getStackpointer() {
        return stackpointer;
    }


    public static int getFromStatus_Register(int index) {
        int toReturn = 0;
        switch (index) {
            case 0: //Carry Flag
                toReturn = (status_Register & 0b0000_0001);
                break;
            case 2: //Digit Carry Flag
                toReturn = (status_Register & 0b0000_0010);
                break;
            case 3: //Zero Flag
                toReturn = (status_Register & 0b0000_0100);
                break;
        }
        return toReturn;
    }


    /**
     * Flag Operations
     **/


    public static void setFlag(int i) {
        switch (i) {
            case 0:
                status_Register = (status_Register | 0b0000_0001);
                break;
            case 1:
                status_Register = (status_Register | 0b0000_0010);
                break;
            case 2:
                status_Register = (status_Register | 0b0000_0100);
                break;
            case 3:
                status_Register = (status_Register | 0b0000_1000);
                break;
        }
    }

    public static void resetFlag(int i) {
        switch (i) {
            case 0:
                status_Register = (status_Register & 0b0000_0001);
                break;
            case 1:
                status_Register = (status_Register & 0b0000_0010);
                break;
            case 2:
                status_Register = (status_Register & 0b0000_0100);
                break;
            case 3:
                status_Register = (status_Register & 0b0000_1000);
                break;
        }
    }

    public void checkForZeroFlag(int result) {
        if (result == 0) {
            setFlag(3);
        }
    }


    public int checkForCarryFlag(int result) {
        if (result < 0 || result > 255) {
            setFlag(0);
            return (result % 256);
        } else {
            return result;
        }
    }

    public int checkForStatusFlags(int result) {
        checkForZeroFlag(result);
        return checkForCarryFlag(result);
    }

    /**
     * File IO Mechanics
     **/

    public void writeToFileRegister(Operation op, int toStore) {
        int adress = op.getFileAddress();
        if ((adress % 128) < 0x0C && (adress % 128) >= 0x00) {
            if (op.getDestinationBit() == 0) {
                switch (op.getFileAddress() % 128) {
                    case 0x0:             //Indirect Addressing
                        ram_Bank0[0] = 0;
                        setFlag(0);
                        break;
                    case 0x1:             //TMR0
                        ram_Bank0[1] = tmr0;
                        break;
                    case 0x02:            //Program Counter Latch Low
                        ram_Bank0[2] = programm_Counter;
                        break;
                    case 0x03:            //Status
                        ram_Bank0[3] = status_Register;
                        break;
                    case 0x04:            //File Save Register
                        ram_Bank0[4] = toStore;
                        break;
                    case 0x05:            //PORT A
                        ram_Bank0[5] = toStore;
                        break;
                    case 0x06:            //PORT B
                        ram_Bank0[6] = toStore;
                        break;
                    case 0x0A:            //Program Counter Latch High
                        ram_Bank0[10] = toStore;
                        break;
                    case 0x0B:            //INTCON
                        break;
                }
            } else if (op.getDestinationBit() == 1) {
                switch (adress % 128) {
                    case 0x00:            //Indirect Addressing
                        ram_Bank1[0] = 0;
                        setFlag(0);
                        break;
                    case 0x01:            //TMR0
                        ram_Bank1[1] = tmr0;
                        break;
                    case 0x02:            //Program Counter Latch Low
                        ram_Bank1[2] = programm_Counter;
                        break;
                    case 0x03:            //Status
                        ram_Bank1[3] = status_Register;
                        break;
                    case 0x04:            //File Save Register
                        ram_Bank1[4] = toStore;
                        break;
                    case 0x05:            //TRIS A
                        ram_Bank1[5] = toStore;
                        break;
                    case 0x06:            //TRIS B
                        ram_Bank1[6] = toStore;
                        break;
                    case 0x0A:            //Program Counter Latch High
                        ram_Bank1[10] = toStore;
                        break;
                    case 0x0B:            //INTCON
                        break;
                }
            }
        } else if ((adress <= 0x80) && ((adress % 128) >= 0x0C)) {
            ram_Bank0[adress % 128] = toStore;
            ram_Bank1[adress % 128] = toStore;

        }
    }

    public void writeToFileRegister(Operation op, int adressToRegister, int toStore) {
        int adress = adressToRegister;
        if ((adress % 128) < 0x0C && (adress % 128) >= 0x00) {
            if (op.getDestinationBit() == 0) {
                switch (op.getFileAddress() % 128) {
                    case 0x0:             //Indirect Addressing
                        ram_Bank0[0] = 0;
                        setFlag(0);
                        break;
                    case 0x1:             //TMR0
                        ram_Bank0[1] = tmr0;
                        break;
                    case 0x02:            //Program Counter Latch Low
                        ram_Bank0[2] = programm_Counter;
                        break;
                    case 0x03:            //Status
                        ram_Bank0[3] = status_Register;
                        break;
                    case 0x04:            //File Save Register
                        ram_Bank0[4] = toStore;
                        break;
                    case 0x05:            //PORT A
                        ram_Bank0[5] = toStore;
                        break;
                    case 0x06:            //PORT B
                        ram_Bank0[6] = toStore;
                        break;
                    case 0x0A:            //Program Counter Latch High
                        ram_Bank0[10] = toStore;
                        break;
                    case 0x0B:            //INTCON
                        break;
                }
            } else if (op.getDestinationBit() == 1) {
                switch (adress % 128) {
                    case 0x00:            //Indirect Addressing
                        ram_Bank1[0] = 0;
                        setFlag(0);
                        break;
                    case 0x01:            //TMR0
                        ram_Bank1[1] = tmr0;
                        break;
                    case 0x02:            //Program Counter Latch Low
                        ram_Bank1[2] = programm_Counter;
                        break;
                    case 0x03:            //Status
                        ram_Bank1[3] = status_Register;
                        break;
                    case 0x04:            //File Save Register
                        ram_Bank1[4] = toStore;
                        break;
                    case 0x05:            //TRIS A
                        ram_Bank1[5] = toStore;
                        break;
                    case 0x06:            //TRIS B
                        ram_Bank1[6] = toStore;
                        break;
                    case 0x0A:            //Program Counter Latch High
                        ram_Bank1[10] = toStore;
                        break;
                    case 0x0B:            //INTCON
                        break;
                }
            }
        } else if ((adress <= 0x80) && ((adress % 128) >= 0x0C)) {
            ram_Bank0[adress % 128] = toStore;
            ram_Bank1[adress % 128] = toStore;

        }
    }

    public int getFromFileRegister(int adress, int destinationBit) {
        int toReturn = 0;
        switch (destinationBit) {
            case 0:
                toReturn = ram_Bank0[adress % 128];
                break;
            case 1:
                switch (adress) {
                    case 3:
                }


                toReturn = ram_Bank1[adress % 128];
                break;
        }
        return toReturn;
    }

    /**
     * Print methods for File Register
     **/

    public void printRegister(int[] arr, int width) {
        int i = 0;
        for (i = 1; i < arr.length; i++) {
            System.out.print(arr[i - 1] + "    ");
            if (i % width == 0) {
                System.out.print("\n");
            }
        }
        System.out.print(arr[i - 1]);
    }

    public static String[][] buildArrayFromFSR(int xSize, int ySize) {
        String[][] arr = new String[xSize][ySize];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                arr[i][j] = String.format("0x%02X", ram_Bank1[j]);            //TODO FIX IT
            }
        }
        return arr;
    }

    public static int[][] buildArray(int[] array, int ySize, int xSize) {
        int[][] arr = new int[ySize][xSize];
        for (int i = 0; i < array.length; i++) {
            arr[i % ySize][i / ySize] = array[i];
        }
        return arr;
    }

    public static Integer[][] buildInteger(int[] array, int ySize, int xSize) {
        Integer[][] arr = new Integer[xSize][ySize];
        for (int i = 0; i < arr.length; i++) {
            arr[i % ySize][i / ySize] = array[i];

        }
        return arr;
    }


    public static void printTwoDimensionalArray(int[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                System.out.printf(String.format("%02Xh ", a[i][j]));
            }
            System.out.println();
        }
    }

    public static int[] getRam_Bank0() {
        return ram_Bank0;
    }

    public static int getIntcon(int i) {
        return intcon[i];
    }

    /**
     * TMR0 Interrupt
     **/


    public void interrupt() {
        push(programm_Counter);
        //Set GIE
        intcon[7] = 1;
    }

    public static int getTmr0() {
        return tmr0;
    }

    public static void setTmr0(int tmr0) {
        Register.tmr0 = tmr0;
    }

    public void incrementTMR0(Operation op, Register reg) {
        int cycles = op.getCycles();
        setTmr0(getTmr0() + cycles);
        checkForTMR0Overflow(reg);
    }

    private void checkForTMR0Overflow(Register reg) {
        if (tmr0 > 255) {
            //Check for Timer enable Bit TOIE
            if (intcon[5] == 1) {
                intcon[2] = 1;
                interrupt();
                setGIE(1);
                System.out.println("TMR0 Overflow");
            }
        }
    }

    /**
     * RB Interrupts
     **/

    public void setGIE(int i) {
        intcon[7] = i;
    }

    public void setGIE() {
        if (intcon[7] == 0) {
            intcon[7] = 1;
        } else {
            intcon[7] = 0;
        }
    }

    public void setRBPC() {
        if (intcon[0] == 0) {
            intcon[4] = 1;
        } else {
            intcon[0] = 0;
        }
    }

    public void setTOIE() {
        if (intcon[5] == 0) {
            intcon[5] = 1;
        } else {
            intcon[5] = 0;
        }
    }

    public void setRBIE() {
        if (intcon[3] == 0) {
            intcon[3] = 1;
        } else {
            intcon[3] = 0;
        }
    }

    public void rb0Interrupt() {
        intcon[1] = 1;
        interrupt();
        setGIE(1);
        System.out.println("RB0 Interrupt");
    }

    public void rbInterrupt() {
        if (intcon[3] == 1) {
            interrupt();
            setGIE(1);
            System.out.println("RB Port Change Interrupt");
        }
    }
}


