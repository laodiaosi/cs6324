#include <iostream>
#include <cmath>
using namespace std;
bool prime(int x)
{
    int y;
    for (y=2;y<=sqrt(x);y++)
    {
        if(x%y==0){
            return false;
        }
    }
    return true;
}
bool cal(int x,int y){
   if(y%x==0&&prime(y/x)){
    return true;
   }
   return false;
}
int main(){
int n,i;
cin>>n;

for(i=3;i<=n;i++){
    if(prime(i)&&cal(i,n))
        cout<<n/i<<endl;
}
return 0;
}
