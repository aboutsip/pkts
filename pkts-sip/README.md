
## Adding a new SIP Header

TODO: just some random notes for now. Will have to finish this eventually..

It is quite often beneficial to create an interface representing a specific SIP header as opposed to stick to the generic SipHeader implementation. Adding a new header to this library is quite easy and this is what you need to do:


* Define a new interface representing your header. Make sure to extend the appropriate base interface. E.g., there are many headers that are so-called “address” headers and in that case, you probably want to extend the AddressParametersHeader interface.
* Your new interface needs to define a static frame method that takes in a Buffer as the single argument and returns your new header.
* All headers are using builders so you need to create an appropriate builder object to go with your header. Builders also inherit from a base build-object classes and there is one for e.g. the AddressParametersHeader.Builder.
* Your new interface also needs to define a copy() method. Everything within this library are immutable so the ONLY way to change something within is to create a copy, which will return a builder, make your changes on that builder and then build the final object.
* On the main SipHeader interface, you need to add two new methods. One isXXX and one toXXXHeader where XXX is the name of your new header. For the isXXX method, please provide a default implementaion that uses the name of the header to figure out if it indeed is an XXX header. Also provide a default toXXXHeader that simply just throws a ClassCastException. Then, on your new header interface, override the isXXX method to return true and the toXXXHeader to return this.
* On your new interface, override the ensure-method to return 'this' and the return value of the signature should be your new interface.
* Create an implementation of your new header and if you e.g. extended the AddressParametersHeader interface then you want to extend the AddressParametersHeaderImpl. 
* Register the new framer with the SipParser. Whenever we parse a header into a more specific header we will see if there is a registered framer to be used and that is stored in a static map on the SipParser. Simply add your framer to the map where the key is the name of your header. Note, if your header has multiple names, such as a short version of the name, then you need to register it twice.  
* Finally, make sure that you add unit tests. No pull request will be accepted without adequate testing. Please add a test to SipHeaderTest.testIsXXXX that
ensures that the isXXX and toXXXHeader methods are working properly.

Here are some headers that are good starting points to study in order to get the all the above correct:

### Address headers
Any Address header such as the ToHeader, FromHeader, ContactHeader etc, whose interface all inherits from the AddressParametersHeader and then the concrete implementation inherits from the   AddressParametersHeaderImpl base class are good places to start. Also note that there are quite many test base classes as well and since address headers are so common, there is an address test base that you should inherit and by doing so, you get a very good coverage right away. See e.g. ToHeaderTest.
