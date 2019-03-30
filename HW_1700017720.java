import java.util.*;
import java.io.*;

public class HW_1700017720 extends Tetris {
    // enter your student id here
    public String id = new String("1700017720");
    boolean current_board[][] = new boolean[h][w];
    int drop_x[];
    int current_x, current_y;

    //计算海拔高度，返回当前块的内
    public int func1(boolean piece[][]) {
        int pos = 4;
        for(int y = 0; y < 4; ++y) {
            for(int x = 0; x < 4; ++x) {
                if(piece[y][x]) {
                    if(pos > y)
                    pos = y;
                }
            }
        }
        return pos;
    }

    //计算消去的贡献
    public int func2(int i, int j, boolean piece[][], boolean new_board[][]) {
        int num1 = 0, num2 = 0;
        for(int y = 0; y < 20; ++y) {
            boolean flag = true;
            for(int x = 0; x < 10; ++x) {
                if(!new_board[y][x]) {
                    flag = false;
                    break;
                }
            }
            if(flag) {
                num1++;
                for(int p = 0; p < 4; ++p) {
                    for(int q = 0; q < 4; ++q) {
                        if(piece[p][q] && j - p == y)
                            num2++;
                    }
                }
            }
        }
        return num1 * num2;
    }

    //计算行变换的值
    public int func3(boolean new_board[][]) {
        int num = 0;
        for(int j = 0; j < 20; ++j) {
            boolean current = new_board[j][0];
            for(int i = 1; i < 10; ++i) {
                if(current != new_board[j][i]) {
                    current = new_board[j][i];
                    num++;
                }
            }
        }
        return num;
    }

    //计算列变换的值
    public int func4(boolean new_board[][]) {
        int num = 0;
        for(int i = 0; i < 10; ++i) {
            boolean current = new_board[0][i];
            for(int j = 1; j < 20; ++j) {
                if(current != new_board[j][i]) {
                    current = new_board[j][i];
                    num++;
                }
            }
        }
        return num;
    }


    //计算空洞的值
    public int func5(boolean new_board[][]) {
        int num = 0;
        for(int i = 0; i < 10; ++i) {
            boolean flag = false;
            for(int j = 19; j >= 0; --j) {
                if(new_board[j][i]) {
                    flag = true;
                    continue;
                }
                if(!new_board[j][i] && flag) {
                    num++;
                }
            }
        }
        return num;
    }

    //计算井的连加值
    public int func6(boolean new_board[][]) {
        int num[] = new int[100];
        int k = 0;
        for(int i = 0; i < 10; ++i) {
            for(int j = 19; j >= 0; --j) {
                if(i == 0) {
                    if(!new_board[j][i] && new_board[j][i+1]) {
                        int l = j;
                        int m = 0;
                        while(l >= 1 && !new_board[l-1][i] && new_board[l-1][i+1]) {
                            l--;
                            m++;
                        }
                        j = l;
                        num[k++] = m;
                    }
                } else if(i == 9) {
                    if(!new_board[j][i] && new_board[j][i-1]) {
                        int l = j;
                        int m = 0;
                        while(l >= 1 && !new_board[l-1][i] && new_board[l-1][i-1]) {
                            l--;
                            m++;
                        }
                        j = l;
                        num[k++] = m;
                    }
                } else {
                    if(!new_board[j][i] && new_board[j][i-1] && new_board[j][i+1]) {
                        int l = j;
                        int m = 0;
                        while(l >= 1 && !new_board[l-1][i] && new_board[l-1][i-1] && new_board[l-1][i+1]) {
                            l--;
                            m++;
                        }
                        j = l;
                        num[k++] = m;
                    }
                }
            }
        }
        int result = 0;
        for(int i = 0; i < k; ++i) {
            while(num[i] > 0) {
                result += num[i];
                num[i]--;
            }
        }
        return result;
    }

    //判断这个状态是否为landed，思路是当前这个状态可以放置，往下一格就不行了，那么这个状态就是landed的状态
    public boolean judgeIfLanded(int x, int y, boolean piece[][], boolean new_board[][]) {
        boolean result = true;
        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < 4; ++j) {
                if(!piece[j][i]) continue;//这是空的，跳过
                else if(new_board[y-j][x+i]) {
                    result = false;
                }
            }
        }

        //判断下一个状态是否可以放置
        boolean flag = false;

        if(result) {
            for(int i = 0; i < 4; ++i) {
                for(int j = 0; j < 4; ++j) {
                    if(!piece[j][i]) continue;
                    else if(y - 1 - j < 0) flag = true;//块到底出界了
                    else if(new_board[y-1-j][x+i]) flag = true;//块下边有东西
                }
            }
        }
        return flag;
    }

    //判断块piece在(y, x)处是否合法，4 * 4的块有可能一部分在外边，但只要有东西的那部分在界内也是可以的
    public boolean outofboard(int x, int y, boolean piece[][]) {
        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < 4; ++j) {
                if(!piece[j][i]) continue;
                else if(x + i < 0 || x + i >= w || y - j < 0 || y - j >= h) return true;
            }
        }
        return false;
    }

    //计算piece的每个合法落点的评估值，枚举更新最优点，把落点的位置保存在drop_x中
    public int caculate(boolean piece[][], int order) {
        boolean new_board[][] = new boolean[h][w];
        int maxvalue = -1000000;
        for(int i = -3; i < 10; ++i) {
            //每次计算前都要把棋盘还原回去
            new_board = getBoard();
            erase(new_board);
            for(int j = current_y; j >= 0; --j) {
                if(outofboard(i, j, piece)) break;
                if(judgeIfLanded(i, j, piece, new_board)) {
                    //move piece to destination
                    for(int x = 0; x < 4; ++x) {
                        for(int y = 0; y < 4; ++y) {
                            if(piece[y][x])  {
                                new_board[j-y][i+x] = true;
                            }
                        }
                    }
                    //evaluate 
                    int value =  -45 * (j - func1(piece))+ 34 * func2(i, j, piece, new_board)  - 32 * func3(new_board) - 93 * func4(new_board) - 79 * func5(new_board) -34 * func6(new_board);
                    if(maxvalue <= value) {
                        maxvalue = value;
                        drop_x[order] = i;        
                    }
                    break;
                }
            }
        }
        return maxvalue;
    }

    public boolean [][] myrotate(boolean piece[][]) {
        boolean tmp[][] = new boolean[4][4];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                tmp[y][x] = piece[x][3-y];
            }
        }
        return tmp;
    }

    public void erase(boolean new_board[][]) {
        boolean piece[][];
        piece = getPiece();
        int piece_x = getPieceX();
        int piece_y = getPieceY();
        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < 4; ++j) {

                if(piece[j][i] && new_board[piece_y-j][piece_x+i]) new_board[piece_y-j][piece_x+i] = false;
            }
        }
    }
    public PieceOperator robotPlay() {
        
        drop_x = new int[4];
        current_x = getPieceX();
        current_y = getPieceY();
        boolean piece0[][];
        boolean piece1[][];
        boolean piece2[][];
        boolean piece3[][];

        //获取当前的块，并生成另外旋转的三种方块
        piece0 = getPiece();
        piece1 = myrotate(piece0);
        piece2 = myrotate(piece1);
        piece3 = myrotate(piece2);

        //分别计算每个块的最大值，如果当前块的值不是最大的，就返回旋转，下几次中的一次传进来的方块应该可以算出一个最大值
        int max0 = caculate(piece0, 0);
        int max1 = caculate(piece1, 1);
        int max2 = caculate(piece2, 2);
        int max3 = caculate(piece3, 3);

        if(max0 < max1 || max0 < max2 || max0 < max3){
            return PieceOperator.Rotate;
        } else {
            if(current_x < drop_x[0]) {
                return PieceOperator.ShiftRight;
            } else if(current_x > drop_x[0]) {
                return PieceOperator.ShiftLeft;
            } else {
                return PieceOperator.Keep;
            }
        }
    }
}




