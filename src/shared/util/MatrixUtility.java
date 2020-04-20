package shared.util;

import shared.data.StaticData;

import java.util.Arrays;

public class MatrixUtility {

    private static final String SEPARATOR = StaticData.matrixSerialisationSeparator;

    /**
     * Pads a two dimensional array with a given value n times.
     * Assumes a square distribution of array entities
     */
    public static int[][] pad2dArray(int[][] array, int n, int value) {
        int[][] newArray = new int[array.length + n * 2][array[0].length + n * 2];
        for(int x = 0; x < newArray.length; x++) {
            for(int y = 0; y < newArray[x].length; y++) {
                if(x < n || y < n
                        || x >= array.length + n
                        || y >= array[0].length + n) newArray[x][y] = value;
                else newArray[x][y] = array[x - n][y - n];
            }
        }
        return newArray;
    }

    public static int[][] pad2dArray(int[][] array, int n, int value, int face) {
        return array;
    }

    /**
     * Fits a 2d array within the given width and height boundaries. Crops the excess data if larger
     * than the bounds, pads the data if smaller than the bounds.
     */
    public static int[][] fit(int[][] array, int width, int height) {
        if(width <= 0 || height <= 0) return new int[0][0];
        int[][] newArray = new int[width][height];

        if(array.length < height) {

        }

        return newArray;
    }

    private static int[][] fitVertical(int[][] array, int height) {
        if(height > array.length) {
            do {

            } while (height > array.length);
            int[] row = new int[array[0].length];
        }
        return array;
    }

    public static int[] getRow(int[][] array, int rowIndex) {
        return array[rowIndex];
    }

    public static int[] getFirstRow(int[][] array) {
        return getRow(array, 0);
    }

    public static int[] getLastRow(int[][] array) {
        return getRow(array, array.length-1);
    }

    public static int[] getCol(int[][] array, int colIndex) {
        int[] col = new int[array.length];
        for(int i = 0; i < array.length; i++) {
            col[i] = array[i][colIndex];
        }
        return col;
    }

    public static int[] getFirstCol(int[][] array) {
        return getCol(array, 0);
    }

    public static int[] getLastCol(int[][] array) {
        return getCol(array, array[0].length-1);
    }

    //  TODO: add some kind of check for array length, all very trusting and crash-prone right now!
    public static int[][] setCol(int[][] array, int colIndex, int value) {
        int[][] newArray = copy(array);
        for(int i = 0; i < newArray.length; i++) {
            newArray[i][colIndex] = value;
        }
        return newArray;
    }

    public static int[][] setCol(int[][] array, int colIndex, int[] col, int alignment) {
        int[] workingCol = new int[array.length];
        if(alignment == Alignment.TOP) {
            for(int i = 0; i < array.length; i++) {
                if(i < col.length) workingCol[i] = col[i];
                else workingCol[i] = 0;
            }
        }
        else if(alignment == Alignment.BOTTOM) {
            for(int i = 0; i < array.length; i++) {
                if(array.length - i <= col.length) workingCol[i] = col[(col.length) - (array.length - i)];
                else workingCol[i] = 0;
            }
        }
        else {
            int padding = (int)(Math.ceil((double)(array.length - col.length)/2.0));
            for(int i = 0; i < array.length; i++) {
                if(i < padding) workingCol[i] = 0;
                else if (i >= padding + col.length) workingCol[i] = 0;
                else workingCol[i] = col[i-padding];
            }
        }

        int[][] newArray = copy(array);
        for(int i = 0; i < newArray.length; i++) {
            newArray[i][colIndex] = workingCol[i];
        }
        return newArray;
    }

    public static int[][] setRow(int[][] array, int rowIndex, int value) {
        int[][] newArray = copy(array);
        Arrays.fill(newArray[rowIndex], value);
        return newArray;
    }

    public static int[][] setRow(int[][] array, int rowIndex, int[] row, int alignment) {
        int[] workingRow = new int[array[0].length];
        if(alignment == Alignment.LEFT) {
            for(int i = 0; i < array[0].length; i++) {
                if(i < row.length) workingRow[i] = row[i];
                else workingRow[i] = 0;
            }
        }
        else if(alignment == Alignment.RIGHT) {
            for(int i = 0; i < array[0].length; i++) {
                if(array[0].length - i <= row.length) workingRow[i] = row[(row.length) - (array[0].length - i)];
                else workingRow[i] = 0;
            }
        }
        else {
            int padding = (int)(Math.ceil((double)(array[0].length - row.length)/2.0));
            for(int i = 0; i < array[0].length; i++) {
                if(i < padding) workingRow[i] = 0;
                else if (i >= padding + row.length) workingRow[i] = 0;
                else workingRow[i] = row[i-padding];
            }
        }

        int[][] newArray = copy(array);
        System.arraycopy(Arrays.copyOf(workingRow, array[rowIndex].length), 0, newArray[rowIndex], 0, array[rowIndex].length);
        return newArray;
    }

    public static int[][] appendRowBefore(int[][] array, int[] row, int alignment) {
        return insertRowAt(array, row, 0, alignment);
    }

    public static int[][] appendRowAfter(int[][] array, int[] row, int alignment) {
        return insertRowAt(array, row, array.length, alignment);
    }

    public static int[][] appendColBefore(int[][] array, int[] col, int alignment) {
        return insertColAt(array, col, 0, alignment);
    }

    public static int[][] appendColAfter(int[][] array, int[] col, int alignment) {
        return insertColAt(array, col, array[0].length, alignment);
    }

    public static int[][] insertRowAt(int[][] array, int[] row, int rowIndex, int alignment) {
        if(rowIndex >= array.length + 1
                || row.length > array[0].length) throw new ArrayIndexOutOfBoundsException();
        int[] workingRow = new int[array[0].length];
        if(alignment == Alignment.LEFT) {
            for(int i = 0; i < array[0].length; i++) {
                if(i < row.length) workingRow[i] = row[i];
                else workingRow[i] = 0;
            }
        }
        else if(alignment == Alignment.RIGHT) {
            for(int i = 0; i < array[0].length; i++) {
                if(array[0].length - i <= row.length) workingRow[i] = row[(row.length) - (array[0].length - i)];
                else workingRow[i] = 0;
            }
        }
        else {
            int padding = (int)(Math.ceil((double)(array[0].length - row.length)/2.0));
            for(int i = 0; i < array[0].length; i++) {
                if(i < padding) workingRow[i] = 0;
                else if (i >= padding + row.length) workingRow[i] = 0;
                else workingRow[i] = row[i-padding];
            }
        }
        int[][] newArray = new int[array.length+1][array[0].length];
        for(int x = 0; x < newArray.length; x++) {
            for(int y = 0; y < newArray[x].length; y++) {
                if(x < rowIndex) newArray[x][y] = array[x][y];
                else if(x == rowIndex) newArray[x][y] = workingRow[y];
                else newArray[x][y] = array[x-1][y];
            }
        }
        return newArray;
    }

    public static int[][] insertColAt(int[][] array, int[] col, int colIndex, int alignment) {
        if(colIndex >= array[0].length + 1
                || col.length > array.length) throw new ArrayIndexOutOfBoundsException();
        int[] workingRow = new int[array.length];
        if(alignment == Alignment.TOP) {
            for(int i = 0; i < array.length; i++) {
                if(i < col.length) workingRow[i] = col[i];
                else workingRow[i] = 0;
            }
        }
        else if(alignment == Alignment.BOTTOM) {
            for(int i = 0; i < array.length; i++) {
                if(array.length - i <= col.length) workingRow[i] = col[(col.length) - (array.length - i)];
                else workingRow[i] = 0;
            }
        }
        else {
            int padding = (int)(Math.ceil((double)(array.length - col.length)/2.0));
            for(int i = 0; i < array.length; i++) {
                if(i < padding) workingRow[i] = 0;
                else if (i >= padding + col.length) workingRow[i] = 0;
                else workingRow[i] = col[i-padding];
            }
        }
        int[][] newArray = new int[array.length][array[0].length+1];
        for(int x = 0; x < newArray.length; x++) {
            for(int y = 0; y < newArray[x].length; y++) {
                if(y < colIndex) newArray[x][y] = array[x][y];
                else if(y == colIndex) newArray[x][y] = workingRow[x];
                else newArray[x][y] = array[x][y-1];
            }
        }
        return newArray;
    }

    /*public static int[][] setRowAt(int[][] array, int[] row, int rowIndex, int alignment) {
        int[] workingRow = new int[array[0].length];
        if(alignment == Alignment.LEFT) {
            for(int i = 0; i < array[0].length; i++) {
                if(i < row.length) workingRow[i] = row[i];
                else workingRow[i] = 0;
            }
        }
        else if(alignment == Alignment.RIGHT) {
            for(int i = 0; i < array[0].length; i++) {
                if(array[0].length - i <= row.length) workingRow[i] = row[(row.length) - (array[0].length - i)];
                else workingRow[i] = 0;
            }
        }
        else {
            int padding = (int)(Math.ceil((double)(array[0].length - row.length)/2.0));
            for(int i = 0; i < array[0].length; i++) {
                if(i < padding) workingRow[i] = 0;
                else if (i >= padding + row.length) workingRow[i] = 0;
                else workingRow[i] = row[i-padding];
            }
        }

        int[][] newArray = new int[array.length][array[0].length];
        for(int x = 0; x < newArray.length; x++) {
            for(int y = 0; y < newArray[x].length; y++) {
                if(x < rowIndex) newArray[x][y] = array[x][y];
                else if(x == rowIndex) newArray[x][y] = workingRow[y];
                else newArray[x][y] = array[x-1][y];
            }
        }
        return newArray;
    }*/

    //  TODO: FINISH!!!
    public static int[][] inject(int[][] source, int[][] injection, int hAlignment, int vAlignment) {
        int[][] newArray = new int[source.length][source[0].length];

        boolean injectionVerticalOverflow = injection.length > source.length;
        boolean injectionHorizontalOverflow = injection[0].length > source[0].length;

        int hPad = Math.abs(source[0].length - injection[0].length);
        int vPad = Math.abs(source.length - injection.length);
        System.out.println(hPad);

        if(injectionHorizontalOverflow) {
            int[] injectableRow = Arrays.copyOfRange(injection[0],(int)Math.floor(hPad/2),injection[0].length-(int)Math.floor(hPad/2));
            printArray(injectableRow);
        }

        return newArray;
    }

    public static void printArray(int[] array) {
        for(int i : array) {
            System.out.print(i + " ");
        }
        System.out.print("\n");
    }

    public static void print2dArray(int[][] array) {
        for(int x = 0; x < array.length; x++) {
            for(int y = 0; y < array[0].length; y++) {
                System.out.print(array[x][y] + " ");
            }
            System.out.print("\n");
        }
    }

    /**
     * gets the cells surrounding a given [x][y] coordinate in an
     * n-radius
     */
    public static int[][] getNeighbours(int[][] array, int n, int x, int y) {
        if(x >= array.length || y >= array[0].length
                || x < 0 || y < 0) {
            throw new ArrayIndexOutOfBoundsException("Co-ordinates out of bounds, x: " + x + " array width: " + array.length + " y: " + y + " array height: " + array[0].length);
        }
        if(x + n >= array.length || y + n >= array[0].length
                || x - n < 0 || y - n < 0) {
            throw new ArrayIndexOutOfBoundsException(n + "-Tile out of bounds");
        }

        int[][] newArray = new int[(2*n)+1][(2*n)+1];
        int xCounter = 0;
        for(int xArray = x - n; xArray <= x + n; xArray++) {
            int yCounter = 0;
            for(int yArray = y - n; yArray <= y + n; yArray++) {
                newArray[xCounter][yCounter++] = array[xArray][yArray];
            }
            xCounter++;
        }
        return newArray;
    }

    public static int sumArray(int[] array) {
        int sum = 0;
        for (int value : array) sum += value;
        return sum;
    }

    public static int sum2dArray(int[][] array) {
        int sum = 0;
        for (int[] rows : array)
            for (int cols : rows) sum += cols;
        return sum;
    }

    public static String toString(int[][] array) {
        String data = "";
        for(int x = 0; x < array.length; x++) {
            for(int y = 0; y < array[x].length; y++) {
                data += array[x][y];
                if(y < array[x].length - 1) data += SEPARATOR;
            }
            data += SEPARATOR + SEPARATOR;
        }
        return data;
    }

    public static String toString(int[] array) {
        String data = "";
        for(int x = 0; x < array.length; x++) {
            data += array[x];
            if(x < array.length - 1) data += SEPARATOR;
        }
        return data;
    }

    public static int[][] matrixFromString(String data) {
        String[] rows = data.split((SEPARATOR + SEPARATOR));
        int[][] matrix = new int[rows.length][];
        for(int i = 0; i < rows.length; i++) {
            String[] rawData = rows[i].split(SEPARATOR);
            int[] litData = new int[rawData.length];
            for(int j = 0; j < rawData.length; j++) {
                if(rawData[j] == null || rawData[j].isEmpty()) continue;
                litData[j] = Integer.parseInt(rawData[j]);
            }
            matrix[i] = litData;
        }
        return matrix;
    }

    public static int[] arrayFromString(String data) {
        String[] rawData = data.split(SEPARATOR);
        int[] litData = new int[rawData.length];
        for(int i = 0; i < rawData.length; i++) {
            if(rawData[i] == null || rawData[i].isEmpty()) continue;
            litData[i] = Integer.parseInt(rawData[i]);
        }
        return litData;
    }

    public static int[][] copy(int[][] data) {
        int[][] copiedData = new int[data.length][];
        for(int i = 0; i < data.length; i++) {
            copiedData[i] = new int[data[i].length];
            System.arraycopy(data[i],0,copiedData[i],0,data[i].length);
        }
        return copiedData;
    }

}
