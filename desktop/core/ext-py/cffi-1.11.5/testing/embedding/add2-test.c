#include <stdio.h>

extern int add1(int, int);
extern int add2(int, int, int);


int main(void)
{
    int x, y;
    x = add1(40, 2);
    y = add2(100, -5, -20);
    printf("got: %d %d\n", x, y);
    return 0;
}
