#include "../include/Customer.h"
#include <algorithm>
#include "iostream"

using namespace std;
//----------------------------------Customer----------------------------------
//Constructor:
Customer::Customer(std::string c_name, int c_id):
    name(std::move(c_name)),
    id(c_id)
{}
//Methods:
Customer::~Customer() {}
std::string Customer::getName() const {return name;}
int Customer::getId() const {return id;}

//----------------------------------"sons" Classes----------------------------------
//----------------------------------VEG----------------------------------
//Veg clone
VegetarianCustomer* VegetarianCustomer::Clone(){
    return new VegetarianCustomer(*this);
}
//Constructor:
VegetarianCustomer::VegetarianCustomer(std::string name, int id):
Customer(std::move(name), id),
vegSmallesId(-1),
BVGmostEXPId(-1),
ordered(false)
{}
//Methods:
std::string VegetarianCustomer::toString() const {return std::to_string(this->getId()) + " " + this->getName();}
std::vector<int> VegetarianCustomer::order(const std::vector<Dish> &menu)
{
    std::vector<int> order;
    //if he already ordered no need to run again we have the id's.
    if(!ordered)
    {
        //maxBVGPrice
        int maxBVG(0);
        bool FoundDish(false);
        bool FoundBVG(false);
        //finding smalled veg dish by id + exp BVG id
        for(std::vector<Dish>::const_iterator it=menu.begin();it!=menu.end();++it)
        {
            if((*it).getType()==VEG && (!FoundDish))
            {
                FoundDish=true;
                vegSmallesId=(*it).getId();
            }
            if((*it).getType()==BVG)
            {
                FoundBVG=true;
                if((*it).getPrice()>maxBVG)
                {
                    maxBVG=(*it).getPrice();
                    BVGmostEXPId=(*it).getId();
                }
            }
        }
        ordered=true;
        //If Veg. customer cannot complete his order
        if(!FoundDish || !FoundBVG)
            return order;
        order.push_back(vegSmallesId);
        order.push_back(BVGmostEXPId);
        return order;
    }
    //if he ordered before we have the ids(checking if he can complete the order -> ids!=-1)
    else if(vegSmallesId!=-1 && BVGmostEXPId!=-1)
    {
        order.push_back(vegSmallesId);
        order.push_back(BVGmostEXPId);
        return order;
    }
    return order;
}

//----------------------------------Cheap----------------------------------
//Constructor:
CheapCustomer::CheapCustomer(std::string name, int id):
        Customer(std::move(name), id),
        ordered(false)
{}
//Cheap clone
CheapCustomer* CheapCustomer::Clone(){
    return new CheapCustomer(*this);
}
//Methods:
std::string CheapCustomer::toString() const {return std::to_string(this->getId()) + " " + this->getName();}
std::vector<int> CheapCustomer::order(const std::vector<Dish> &menu) {
    std::vector<int> order;
    //Cheap customer only order once
    if(!ordered) {
        int dishMinID = -1;
        int dishMinPrice = -1;
        if (!menu.empty()) {
            dishMinID = menu.at(0).getId();
            dishMinPrice = menu.at(0).getPrice();
            //Finding min price Dish
        for (std::vector<Dish>::const_iterator it = menu.begin(); it != menu.end(); ++it) {
            if (it->getPrice() < dishMinPrice) {
                dishMinID = it->getId();
                dishMinPrice = it->getPrice();
            }
        }
        order.push_back(dishMinID);
        ordered = true;
        }
    }
    return order;

}

//----------------------------------Spicy----------------------------------
//Constructor:
SpicyCustomer::SpicyCustomer(std::string name, int id):Customer(name,id),isOrder(false),isOrderBVG(false),CheapestBVGdish(0) {}
//Spicy clone
SpicyCustomer* SpicyCustomer::Clone(){
    return new SpicyCustomer(*this);
}
//Methods:
std::string SpicyCustomer::toString() const {return ""+std::to_string(this->getId())+" "+ this->getName();}

bool SpicyCustomer::getisOrder() const {return isOrder;}

void SpicyCustomer::SetisOrder(bool isOrder2) {isOrder=isOrder2;}

bool SpicyCustomer::getIsOrderBVG() const {return isOrderBVG;}

void SpicyCustomer::SetIsOrderBVG(bool isOrderBVG) {this->isOrderBVG=isOrderBVG;}

std::vector<int> SpicyCustomer::order(const std::vector<Dish> &menu)
{
    std::vector<int> order;
    bool FoundDishSPC(false);
    bool FoundDishBVG(false);
    int maxSPC(-1);
    int maxSPCID(-1);
    int CheapestBVG(std::numeric_limits<int>::max());
    int CheapestBVGID(-1);
    //if we already ordered BVG no need to loop to find it again
    if(getIsOrderBVG()){
        order.push_back(CheapestBVGdish);
        return order;
    }
    else{
    for(std::vector<Dish>::const_iterator it=menu.begin();it!=menu.end() && !getisOrder();++it)
    {
        //search for SPC dish first and then BVG
        if((*it).getType()==SPC){
            if((*it).getPrice()>maxSPC) {
                maxSPC = (*it).getPrice();
                maxSPCID = (*it).getId();
            }
            FoundDishSPC=true;
        }
            //search for cheapest BVG
        else if((*it).getType()==BVG)
        {
            if((*it).getPrice()<CheapestBVG)
            {
                CheapestBVG=(*it).getPrice();
                CheapestBVGID=(*it).getId();
            }
            FoundDishBVG=true;
        }
    }
    }
    if(FoundDishSPC && !isOrder)
    {
        order.push_back(maxSPCID);
        SetisOrder(true);
    }
    if(FoundDishBVG && FoundDishSPC)
    {
        CheapestBVGdish=CheapestBVGID;
        SetIsOrderBVG(true);
    }
    return order;
}

//----------------------------------AlC----------------------------------
//Constructor
AlchoholicCustomer::AlchoholicCustomer(std::string name, int id):
        Customer(std::move(name), id),
        firstTime(true),
        DishesIds()
{}
//Alc clone
AlchoholicCustomer* AlchoholicCustomer::Clone(){
    return new AlchoholicCustomer(*this);
}
//Methods:
std::string AlchoholicCustomer::toString() const {return std::to_string(this->getId()) + " " + this->getName();}
std::vector<int> AlchoholicCustomer::order(const std::vector<Dish> &menu) {
    std::vector<int> order;
    std::vector<int> DishesPrices;
    //if its first time ordering we will sort a vector of only ALC types by their price
    if(firstTime)
    {
        for(std::vector<Dish>::const_iterator it=menu.begin();it!=menu.end();++it)
        {
            if(it->getType()==ALC) {
                DishesIds.push_back(it->getId());
                DishesPrices.push_back(it->getPrice());
            }
        }
        //Sorting the ALC Dishes using bubble sort
        for(size_t i=0; i<DishesIds.size();i++)
        {
            for(size_t j=0; j<DishesIds.size()-1;j++)
            {
                if(DishesPrices.at(j)>DishesPrices.at(j+1))
                {
                    int tempPrice = DishesPrices.at(j);
                    int tempId = DishesIds.at(j);
                    DishesPrices.at(j)=DishesPrices.at(j+1);
                    DishesIds.at(j)=DishesIds.at(j+1);
                    DishesPrices.at(j+1)=tempPrice;
                    DishesIds.at(j+1)=tempId;
                }
            }
        }
        firstTime=false;
    }
    if(DishesIds.size()>0)
    {
        order.push_back(DishesIds.at(0));
        DishesIds.erase(DishesIds.begin());
    }
    return order;
}