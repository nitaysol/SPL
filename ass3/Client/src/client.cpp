//
// Created by nitays on 01/01/19.
//

#include <pthread.h>
#include <stdlib.h>
#include <iostream>
#include <connectionHandler.h>
#include <thread>
#include <mutex>
#include <condition_variable>

std::mutex m;
std::condition_variable cond;

using namespace std;
class Listener{
private:
    bool *isTerminated;
    ConnectionHandler *handler;
public:
    Listener(ConnectionHandler *handler1, bool *isTerminated):
    isTerminated(isTerminated),
    handler(handler1)
    {}

    void run(){
        while(!*isTerminated)
        {
            std::string answer;
            char charArr[2];
            handler->getBytes(charArr, 2);
            short opCode = handler->bytesToShort(charArr); //get the opCode
            switch (opCode)
            {
                case 9: { /* ******************* NOTIFICATION ****************** */
                    answer += "NOTIFICATION ";
                    char notificationTypeChar[1];
                    handler->getBytes(charArr, 1);
                    short notificationType = handler->bytesToShort(notificationTypeChar); //get the notification type - 0->PM 1->Public
                    if(notificationType==0){
                        answer+="PM ";
                    }
                    else {
                        answer+="Public ";
                    }
                    string postUser;
                    string message;
                    handler->getLineZeroByte(postUser);
                    handler->getLineZeroByte(message);
                    answer = answer + postUser + " " + message;
                }
                break;
                case 10: { /* ******************* ACK ****************** */
                    answer += "ACK ";
                    handler->getBytes(charArr, 2);
                    short msgOpCode = handler->bytesToShort(charArr);
                    answer += to_string(msgOpCode);
                    if (msgOpCode == 4 || msgOpCode == 7) { //Follow or USERLIST Command
                        handler->getBytes(charArr, 2);
                        short numOfFollowers = handler->bytesToShort(charArr);
                        answer += " " + to_string(numOfFollowers) + " ";
                        for(short i=0;i<numOfFollowers;i++) {
                            string users;
                            handler->getLineZeroByte(users);
                            answer = answer + users;
                            if (i != numOfFollowers-1) answer += " ";
                        }
                    } else if (msgOpCode == 8) { //STAT Command
                        answer += " ";
                        for (int i = 0; i < 3; i++) {
                            handler->getBytes(charArr, 2);
                            answer += to_string(handler->bytesToShort(charArr));
                            if (i != 2) answer += " ";
                        }
                    }
                    else if(msgOpCode==3){//LOGOUT Command
                        *isTerminated=true;
                        handler->close();
                        cond.notify_all();
                    }
                    break;
                }
                case 11: { /* ******************* ERROR ****************** */
                    answer += "ERROR ";
                    handler->getBytes(charArr, 2);
                    short msgOpCodeERR=handler->bytesToShort(charArr);
                    answer += to_string(handler->bytesToShort(charArr));
                    if(msgOpCodeERR==3){//LOGOUT Command
                        cond.notify_all();
                    }
                    break;
                }
            }
            cout<<answer<<endl;
        }
    }

};

class Executor{
private:
    bool *isTerminated;
    ConnectionHandler *handler;
    std::vector<std::string> splitStringBySpaceToVec(std::string s) {
        std::vector<std::string> result;
        std::istringstream iss(s);
        for(std::string s; iss >> s; )
            result.push_back(s);
        return result;
    }
public:
    Executor(ConnectionHandler *handler1, bool *isTerminated):
    isTerminated(isTerminated),
    handler(handler1)
    {}

    void run(){
        while(!*isTerminated){
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);

            vector<string> inputDelimiterBySpace = splitStringBySpaceToVec(line);
            char opCodeArray[2];
            char del(' ');
            string start = inputDelimiterBySpace[0];
            if(start=="REGISTER"){
                handler->shortToBytes(01,opCodeArray);
                handler->sendBytes(opCodeArray, 2);
                handler->sendFrameAscii(inputDelimiterBySpace[1],'\0');
                handler->sendFrameAscii(inputDelimiterBySpace[2],'\0');}
            else if(start=="LOGIN") {
                handler->shortToBytes(02, opCodeArray);
                handler->sendBytes(opCodeArray, 2);
                handler->sendFrameAscii(inputDelimiterBySpace[1], '\0');
                handler->sendFrameAscii(inputDelimiterBySpace[2], '\0');
            }
            else if(start=="LOGOUT") {
                handler->shortToBytes(03, opCodeArray);
                handler->sendBytes(opCodeArray, 2);
                std::unique_lock<std::mutex> lk{m};
                cond.wait(lk);
            }
            else if(start=="FOLLOW") {
                handler->shortToBytes(04, opCodeArray);
                handler->sendBytes(opCodeArray, 2);
                if(inputDelimiterBySpace[1]=="0")
                    handler->sendFrameAscii("", '\0');
                else
                    handler->sendFrameAscii("", '\1');
                char numOfUsers[2];
                handler->shortToBytes(std::stoi(inputDelimiterBySpace[2]), numOfUsers);
                handler->sendBytes(numOfUsers,2);
                for (int i = 3; i < std::stoi(inputDelimiterBySpace[2])+3; i++) {
                    del = '\0';
                    handler->sendFrameAscii(inputDelimiterBySpace[i], del);
                }
            }
            else if(start=="POST") {
                handler->shortToBytes(05, opCodeArray);
                handler->sendBytes(opCodeArray, 2);
                for (size_t i = 1; i < inputDelimiterBySpace.size(); i++) {
                    if (i == inputDelimiterBySpace.size() - 1) del = '\0';
                    handler->sendFrameAscii(inputDelimiterBySpace[i], del);
                }
            }
            else if(start=="PM") {
                handler->shortToBytes(06, opCodeArray);
                handler->sendBytes(opCodeArray, 2);
                handler->sendFrameAscii(inputDelimiterBySpace[1], '\0');
                for (size_t i = 2; i < inputDelimiterBySpace.size(); i++) {
                    if (i == inputDelimiterBySpace.size() - 1) del = '\0';
                    handler->sendFrameAscii(inputDelimiterBySpace[i], del);
                }
            }
            else if(start=="USERLIST") {
                handler->shortToBytes(07, opCodeArray);
                handler->sendBytes(opCodeArray, 2);
            }
            else if(start=="STAT") {
                handler->shortToBytes(8, opCodeArray);
                handler->sendBytes(opCodeArray, 2);
                handler->sendFrameAscii(inputDelimiterBySpace[1], '\0');
            }
            else
                cout<<"INVALID COMMAND"<<endl;

        }
    }
};

int main(int argc, char *argv[]){
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);

    bool isTerminated(false);
   if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    Listener L(&connectionHandler, &isTerminated);
    Executor E(&connectionHandler, &isTerminated);
    std::thread ListenerThread(&Listener::run, L);
    std::thread ExecutorThread(&Executor::run, E);
    ListenerThread.join();
    ExecutorThread.join();
    return 0;
}

