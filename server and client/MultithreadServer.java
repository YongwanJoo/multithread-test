import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadServer {
    private static String getCalculate(String content) {
        //연산자의 종류 저장
        char[] operationCode = {'+', '-', '*', '/', '(', ')'};
        //연산을 위해 정보를 담는 array list 선언
        ArrayList<String> postfixList = new ArrayList<String>();
        Stack<Character> opStack = new Stack<Character>();
        Stack<String> calculatorStack = new Stack<String>(); 

        int index = 0;

        for (int i = 0; i < content.length(); i++) {
            for (int j = 0; j < operationCode.length; j++) {
                //연산자인지 확인
                if (content.charAt(i) == operationCode[j]) { 
                    
                    //괄호가 나온경우 괄호를 우선으로 처리하기 위한 식 설정
                    postfixList.add(content.substring(index, i).trim().replace("(", "").replace(")", ""));
                    if (content.charAt(i) == '(') {
                        if (content.charAt(i) == ')') {
                            while (true) {
                                postfixList.add(opStack.pop().toString());
                                if (opStack.pop() == '(' || opStack.isEmpty()) {
                                    break;
                                }
                            }
                        }
                    }
                    //스택에 연산 정보를 담음
                    if (opStack.isEmpty()) { 
                        opStack.push(operationCode[j]);
                    } else { 
                        if (opOrder(operationCode[j]) > opOrder(opStack.peek())) { 
                            opStack.push(operationCode[j]); 
                        } else if (opOrder(operationCode[j]) <= opOrder(opStack.peek())) {
                            postfixList.add(opStack.peek().toString());
                            opStack.pop();
                            opStack.push(operationCode[j]);
                            }
                    }
                    index = i + 1;
                }
            }
        }
        postfixList.add(content.substring(index, content.length()).trim().replace("(", "").replace(")", ""));

        if (!opStack.isEmpty()) { 
            for (int i = 0; i < opStack.size();) {
                postfixList.add(opStack.peek().toString());
                opStack.pop();
            }
        }

        //괄호를 우선순위로 두었으므로 괄호 제거
        for (int i = 0; i < postfixList.size(); i++) {
            if (postfixList.get(i).equals("")) {
                postfixList.remove(i);
                i = i - 1;
            } else if (postfixList.get(i).equals("(")) {
                postfixList.remove(i);
                i = i - 1;
            } else if (postfixList.get(i).equals(")")) {
                postfixList.remove(i);
                i = i - 1;
            }
        }

        System.out.println(postfixList);

        opStack.clear(); 

        //입력받은 postfixList를 활용해서 연산수행
        for (int i = 0; i < postfixList.size(); i++) {
            calculatorStack.push(postfixList.get(i));
            for (int j = 0; j < operationCode.length; j++) {
                if (postfixList.get(i).charAt(0) == operationCode[j]) { 
                    calculatorStack.pop(); 
                    double s2, s1; 
                    String rs; 

                    s2 = Double.parseDouble(calculatorStack.pop()); 
                    s1 = Double.parseDouble(calculatorStack.pop());

                    
                    switch (operationCode[j]) {
                        case '+':
                            rs = String.valueOf(s1 + s2);
                            calculatorStack.push(rs);
                            break;
                        case '-':
                            rs = String.valueOf(s1 - s2);
                            calculatorStack.push(rs);
                            break;
                        case '*':
                            rs = String.valueOf(s1 * s2);
                            calculatorStack.push(rs);
                            break;
                        case '/':
                            if (s2 == 0) {
                                System.out.println("In correct: divided by zero");
                                break;
                            }
                            rs = String.valueOf(s1 / s2);
                            calculatorStack.push(rs);
                            break;
                    }
                }
            }
        }
        //연산 수행 결과를 변수에 담아줌
        double re = Double.parseDouble(calculatorStack.peek()); 
        String result = String.format("%.10f", re); 

        //소수점 자리 처리        
        int num = 0;
        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == '.') {
                num = i;
                break;
            }
        }

        
        String mok = result.substring(0, num);

        
        double divde = Double.parseDouble(result) % Double.parseDouble(mok);

        
        if (divde == 0) {
            result = String.format("%.0f", re);
        }

        return result;
    }
    //연산의 경우 우선순위가 있으므로 우선순위를 정해주는 함수 설정
    public static int opOrder(char op) {
        switch (op) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return -1;
        }
    }
    public static void main(String[] args) {
        int port = 7777;
        ExecutorService executor = Executors.newFixedThreadPool(20);

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept(); 
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                executor.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
    
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
    
        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
    
                while (true) {
                    String inputLine = input.readLine();
                    //bye가 나오기 전까지 접속 유지
                    if (inputLine == null || inputLine.equalsIgnoreCase("bye")) {
                        System.out.println("Client close connection");
                        break;
                    }
                    System.out.println(inputLine);
    
                    String result = getCalculate(inputLine);
                    output.println(result);
    
                    saveResultToFile(result);
                }
    
                clientSocket.close();
                System.out.println("Connection closed for " + clientSocket.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        private void saveResultToFile(String result) {
            try {
                File file = new File("calculate.txt");
                FileWriter writer = new FileWriter(file, true); 
                writer.write(result + System.lineSeparator()); 
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }    
}
