#ifndef CUSTOMER_H_
#define CUSTOMER_H_

#include <vector>
#include <string>
#include "Dish.h"

class Customer{
public:
    Customer(std::string c_name, int c_id);
    virtual std::vector<int> order(const std::vector<Dish> &menu)=0;
    virtual std::string toString() const = 0;
    virtual Customer* Clone()=0;
    std::string getName() const;
    int getId() const;
    virtual ~Customer();
private:
    const std::string name;
    const int id;
};


class VegetarianCustomer : public Customer {
public:
    virtual ~VegetarianCustomer() = default;
	VegetarianCustomer(std::string name, int id);
    std::vector<int> order(const std::vector<Dish> &menu);
    std::string toString() const;
private:
    VegetarianCustomer* Clone();
    int vegSmallesId;
    int BVGmostEXPId;
    bool ordered;
};


class CheapCustomer : public Customer {
public:
    virtual ~CheapCustomer() = default;
	CheapCustomer(std::string name, int id);
    std::vector<int> order(const std::vector<Dish> &menu);
    std::string toString() const;
private:
	bool ordered;
    CheapCustomer* Clone();
};

class SpicyCustomer : public Customer {
public:
	virtual ~SpicyCustomer() = default;
    SpicyCustomer(std::string name, int id);
    std::vector<int> order(const std::vector<Dish> &menu);
    std::string toString() const;
    bool getisOrder() const;
    void SetisOrder(bool isOrder);
private:
    bool isOrder;
    bool isOrderBVG;
    bool getIsOrderBVG() const;
    void SetIsOrderBVG(bool isOrderBVG);
    SpicyCustomer* Clone();
    int CheapestBVGdish;
};



class AlchoholicCustomer : public Customer {
public:
    virtual ~AlchoholicCustomer() = default;
	AlchoholicCustomer(std::string name, int id);
    std::vector<int> order(const std::vector<Dish> &menu);
    std::string toString() const;
private:
    bool firstTime;
    AlchoholicCustomer* Clone();
    std::vector<int> DishesIds;


};


#endif
