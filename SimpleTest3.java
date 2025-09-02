// import gov.nasa.jpf.vm.Verify; //Potentially what we can use to force checkpoints

// //The idea here is to use Verify.getBoolean() to force a checkpoint.

// public class SimpleTest3 {

//     static int a = 0;
//     static int b = 0;
//     static int x = 0;
//     static int y = 0;

//     public static void main(String[] args) throws InterruptedException {
//         for (int i = 0; i < 1; i++) {

//             a = b = x = y = 0;

//             Thread t1 = new Thread(() -> {
//                 a = 1;
//                 Verify.getBoolean();
//                 x = b;
//             });

//             Thread t2 = new Thread(() -> {
//                 b = 1;
//                 Verify.getBoolean();
//                 y = a;
//             });

//             t1.start();
//             t2.start();

//             t1.join();
//             t2.join();

//             // if (x == 0 && y == 0) {
//             // System.out.println("Found unwanted interleaving: x=0, y=0 at iteration " +
//             // i);
//             // break;
//             // }

//             assert x == 1 && y == 1 : "Interleaving exposes a bug where x = " + x + " and y = " + y;

//         }
//     }
// }
