pragma solidity ^0.4.23;



//////////
//////////
//////////          Library SafeMath
//////////
//////////



/**
 * @title SafeMath
 * @dev Math operations with safety checks that throw on error
 */
library SafeMath {

  /**
  * @dev Multiplies two numbers, throws on overflow.
  */
    function mul(uint256 a, uint256 b) internal pure returns (uint256) {
        if (a == 0) {
            return 0;
    }
        uint256 c = a * b;
        assert(c / a == b);
        return c;
    }

  /**
  * @dev Integer division of two numbers, truncating the quotient.
  */
    function div(uint256 a, uint256 b) internal pure returns (uint256) {
        // assert(b > 0); // Solidity automatically throws when dividing by 0
        uint256 c = a / b;
        // assert(a == b * c + a % b); // There is no case in which this doesn't hold
        return c;
    }

  /**
  * @dev Subtracts two numbers, throws on overflow (i.e. if subtrahend is greater than minuend).
  */
    function sub(uint256 a, uint256 b) internal pure returns (uint256) {
        assert(b <= a);
        return a - b;
    }

  /**
  * @dev Adds two numbers, throws on overflow.
  */
    function add(uint256 a, uint256 b) internal pure returns (uint256) {
        uint256 c = a + b;
        assert(c >= a);
        return c;
    }
}




//////////
//////////
//////////          Contract Ownable
//////////
//////////


/**
 * @title Ownable
 * @dev The Ownable contract has an owner address, and provides basic authorization control
 * functions, this simplifies the implementation of "user permissions".
 */
contract Ownable {

    /// @dev Emited when contract is upgraded - See README.md for updgrade plan
    event ContractUpgrade(address newContract);

    address public owner;

    // @dev Keeps track whether the contract is paused. When that is true, most actions are blocked
    bool public paused = false;


    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);

  /**
   * @dev The Ownable constructor sets the original `owner` of the contract to the sender
   * account.
   */
    /*function Ownable() public {
        owner = msg.sender;
    }*/
    constructor() public{
        owner = msg.sender;
    }


  /**
   * @dev Throws if called by any account other than the owner.
   */
    modifier onlyOwner() {
        require(msg.sender == owner);
        _;
    }


       /*** Pausable functionality adapted from OpenZeppelin ***/

    /// @dev Modifier to allow actions only when the contract IS NOT paused
    modifier whenNotPaused() {
        require(!paused);
        _;
    }

    /// @dev Modifier to allow actions only when the contract IS paused
    modifier whenPaused {
        require(paused);
        _;
    }

    /// @dev Called by any "C-level" role to pause the contract. Used only when
    ///  a bug or exploit is detected and we need to limit damage.
    function pause() external onlyOwner whenNotPaused {
        paused = true;
    }


  /**
   * @dev Allows the current owner to transfer control of the contract to a newOwner.
   * @param newOwner The address to transfer ownership to.
   */
    function transferOwnership(address newOwner) public onlyOwner {
        require(newOwner != address(0));
        emit OwnershipTransferred(owner, newOwner);
        owner = newOwner;
    }

}





//////////
//////////
//////////          Contract FishFactory
//////////
//////////


contract FishFactory is Ownable {

    using SafeMath for uint256;

    /*** EVENTS ***/

    /// @dev The Birth event is fired whenever a new fish comes into existence. This obviously
    ///  includes any time a fish is created through the giveBirth method, but it is also called
    ///  when a new gen0 fish is created.
    event Birth(address owner, uint32 fishId, uint32 matronId, uint32 sireId, uint256 genes);

    /// @dev Transfer event as defined in current draft of ERC721. Emitted every time a fish
    ///  ownership is assigned, including births.
    event Transfer(address from, address to, uint256 tokenId);


    /*** DATA TYPES ***/

    /// @dev The main Fish struct. Every fish in CryptoFish is represented by a copy
    ///  of this structure, so great care was taken to ensure that it fits neatly into
    ///  exactly two 256-bit words. Note that the order of the members in this structure
    ///  is important because of the byte-packing rules used by Ethereum.
    ///  Ref: http://solidity.readthedocs.io/en/develop/miscellaneous.html
    struct Fish {
  

        // The Fish's genetic code is packed into these 256-bits, the format is
        // sooper-sekret! A fish's genes never change.
        uint256 genes;

        // The timestamp from the block when this Fish came into existence.
        uint64 birthTime;

        // The minimum timestamp after which this cat can engage in breeding
        // activities again. This same timestamp is used for the pregnancy
        // timer (for matrons) as well as the siring cooldown.
        uint64 cooldownEndBlock;

        // The ID of the parents of this Fish, set to 0 for gen0 Fishes.
        // Note that using 32-bit unsigned integers limits us to a "mere"
        // 4 billion Fishes. This number might seem small until you realize
        // that Ethereum currently has a limit of about 500 million
        // transactions per year! So, this definitely won't be a problem
        // for several years (even as Ethereum learns to scale).
        uint32 matronId;
        uint32 sireId;

        // Set to the ID of the sire Fish for matrons that are pregnant,
        // zero otherwise. A non-zero value here is how we know a Fish
        // is pregnant. Used to retrieve the genetic material for the new
        // Fish when the birth transpires.
        uint32 siringWithId;

        // Set to the index in the cooldown array (see below) that represents
        // the current cooldown duration for this Fish. This starts at zero
        // for gen0 Fishes, and is initialized to floor(generation/2) for others.
        // Incremented by one for each successful breeding action, regardless
        // of whether this Fish is acting as matron or sire.
        uint16 cooldownIndex;

        // The "generation number" of this Fish. Fishes minted by the CK contract
        // for sale are called "gen0" and have a generation number of 0. The
        // generation number of all other Fishes is the larger of the two generation
        // numbers of their parents, plus one.
        // (i.e. max(matron.generation, sire.generation) + 1)
        uint16 generation;
    
    }

    /*** CONSTANTS ***/

    /// @dev A lookup table indiFishing the cooldown duration after any successful
    ///  breeding action, called "pregnancy time" for matrons and "siring cooldown"
    ///  for sires. Designed such that the cooldown roughly doubles each time a Fish
    ///  is bred, encouraging owners not to just keep breeding the same Fish over
    ///  and over again. Caps out at one week (a Fish can breed an unbounded number
    ///  of times, and the maximum cooldown is always seven days).
    uint32[14] public cooldowns = [
        uint32(1 minutes),
        uint32(2 minutes),
        uint32(5 minutes),
        uint32(10 minutes),
        uint32(30 minutes),
        uint32(1 hours),
        uint32(2 hours),
        uint32(4 hours),
        uint32(8 hours),
        uint32(16 hours),
        uint32(1 days),
        uint32(2 days),
        uint32(4 days),
        uint32(7 days)
    ];


    // An approximation of currently how many seconds are in between blocks.
    uint256 public secondsPerBlock = 15;


    /*** STORAGE ***/
    /// @dev An array containing the Fish struct for all Fishes in existence. The ID
    ///  of each Fish is actually an index into this array. Note that ID 0 is a negaFish,
    ///  the unFish, the mythical beast that is the parent of all gen0 Fishes. A bizarre
    ///  creature that is both matron and sire... to itself! Has an invalid genetic code.
    ///  In other words, Fish ID 0 is invalid... ;-)
    Fish[] public fishes;

    /// @dev A mapping from Fish IDs to the address that owns them. All Fishes have
    ///  some valid owner address, even gen0 Fishes are created with a non-zero owner.
    mapping (uint256 => address) public fishToOwner;

    // @dev A mapping from owner address to count of tokens that address owns.
    //  Used internally inside balanceOf() to resolve ownership count.
    mapping (address => uint256) public ownerFishCount;

    /// @dev A mapping from FishIDs to an address that has been approved to call
    ///  transferFrom(). Each Fish can only have one approved address for transfer
    ///  at any time. A zero value means no approval is outstanding.
    mapping (uint256 => address) public fishApprovals;

    /// @dev A mapping from FishIDs to an address that has been approved to use
    ///  this Fish for siring via breedWith(). Each Fish can only have one approved
    ///  address for siring at any time. A zero value means no approval is outstanding.
    mapping (uint256 => address) public sireAllowedAddress;

    /// @dev Assigns ownership of a specific Fish to an address.
    function _transfer(address _from, address _to, uint256 _tokenId) public {
        // Since the number of fishes is capped to 2^32 we can't overflow this

        ownerFishCount[_to]=ownerFishCount[_to].add(1);
        // transfer ownership
        fishToOwner[_tokenId] = _to;
        
        if (_from != 0) {
            ownerFishCount[_from]=ownerFishCount[_from].sub(1);
            
            // once the fish is transferred also clear sire allowances
            delete sireAllowedAddress[_tokenId];
            // clear any previously approved ownership exchange
            delete fishApprovals[_tokenId];
        }
        // Emit the transfer event.
        emit Transfer(_from, _to, _tokenId);
    }





    function _createFish(
        uint256 _genes,
        uint32 _matronId, 
        uint32 _sireId, 
        uint16 _generation,
        address _owner
        ) 
        public
        returns (uint256)
    {
        // New Fish starts with the same cooldown as parent gen/2
        uint16 cooldownIndex = uint16(_generation / 2);
        if (cooldownIndex > 13) {
            cooldownIndex = 13;
        }

        Fish memory _fish = Fish({
            genes: _genes,
            birthTime: uint64(block.timestamp),
            cooldownEndBlock: 0,
            matronId: uint32(_matronId),
            sireId: uint32(_sireId),
            siringWithId: 0,
            cooldownIndex: cooldownIndex,
            generation: uint16(_generation)
        });
        uint256 _newFishId = fishes.push(_fish) - 1;

        // It's probably never going to happen, 4 billion fishes is A LOT, but
        // let's just be 100% sure we never let this happen.
        uint32 newFishId = (uint32) (_newFishId);
        //require(_newFishId == uint256(newFishId));

        // emit the birth event
        emit Birth(
            _owner,
            newFishId,
            _fish.matronId,
            _fish.sireId,
            _fish.genes
        );

        // This will assign ownership, and also emit the Transfer event as
        // per ERC721 draft
        _transfer(0, _owner, newFishId);

        return newFishId;
    }


}



//////////
//////////
//////////          Contract ERC721
//////////
//////////



contract ERC721 {
    event Transfer(address indexed _from, address indexed _to, uint256 _tokenId);
    event Approval(address indexed _owner, address indexed _approved, uint256 _tokenId);

    function name() public view returns (string _name);
    function symbol() public view returns (string _symbol);
    function totalSupply() public view returns(uint256 _supply);
    function balanceOf(address _owner) public view returns (uint256 _balance);
    function ownerOf(uint256 _tokenId) public view returns (address _owner);
    function transfer(address _to, uint256 _tokenId) public;
    function approve(address _to, uint256 _tokenId) public;
    function takeOwnership(uint256 _tokenId) public;
}


//////////
//////////
//////////          Contract FishOwnership
//////////
//////////



contract FishOwnership is FishFactory, ERC721 {

    /// @notice Name and symbol of the non fungible token, as defined in ERC721.
    string public constant token_name = "CryptoFancyCarps";
    string public constant token_symbol = "CFC";

    using SafeMath for uint256;

    modifier onlyOwnerOf(uint _tokenId) {
        require(msg.sender == fishToOwner[_tokenId]);
        _;
    }

    modifier onlyApprovedOf(uint _tokenId){
        require(msg.sender == fishApprovals[_tokenId]);
        _;
    }

    /// implementions of ERC721

    function name() public view returns(string _name){
        return token_name;
    }

    function symbol() public view returns (string _symbol){
        return token_symbol;
    }

    function balanceOf(address _owner) public view returns (uint256 _balance) {
        return ownerFishCount[_owner];
    }

    function ownerOf(uint256 _tokenId) public view returns (address _owner) {
        return fishToOwner[_tokenId];
    }

    /// @notice Returns the total number of Fishes currently in existence.
    /// @dev Required for ERC-721 compliance.
    function totalSupply() public view returns (uint) {
        return fishes.length;
    }


    function approve(address _to, uint256 _tokenId) public whenNotPaused onlyOwnerOf(_tokenId) {
        fishToOwner[_tokenId] = _to;
        emit Approval(msg.sender, _to, _tokenId);
    }

    function takeOwnership(uint256 _tokenId) public whenNotPaused onlyApprovedOf(_tokenId){
        address owner = ownerOf(_tokenId);
        _transfer(owner, msg.sender, _tokenId);
    }

    /// @notice Returns a list of all Fish IDs assigned to an address.
    /// @param _owner The owner whose Fishes we are interested in.
    /// @dev This method MUST NEVER be called by smart contract code. First, it's fairly
    ///  expensive (it walks the entire Fish array looking for fishes belonging to owner),
    ///  but it also returns a dynamic array, which is only supported for web3 calls, and
    ///  not contract-to-contract calls.
    function tokensOfOwner(address _owner) external view returns(uint256[] ownerTokens) {
        uint256 tokenCount = balanceOf(_owner);

        if (tokenCount == 0) {
            // Return an empty array
            return new uint256[](0);
        } else {
            uint256[] memory result = new uint256[](tokenCount);
            uint256 totalfishs = totalSupply();
            uint256 resultIndex = 0;

            // We count on the fact that all fishes have IDs starting at 1 and increasing
            // sequentially up to the totalfish count.
            uint256 fishId;

            for (fishId = 1; fishId < totalfishs; fishId++) {
                if (fishToOwner[fishId] == _owner) {
                    result[resultIndex] = fishId;
                    resultIndex++;
                }
            }

            return result;
        }
    }
}



//////////
//////////
//////////          Contract FishSale
//////////
//////////



contract FishSale is FishOwnership {
    // Represents an sale on an NFT
    struct Sale {
        // Current owner of NFT
        address seller;
        // Price (in wei) of sale
        uint128 price;
        // Time when sale started
        // NOTE: 0 if this sale has been concluded
        uint64 startedAt;
    }
    // NFT
    mapping (uint256 => Sale) public tokenIdToSale;

    event SaleCreated(uint256 tokenId, uint256 price);
    event SaleSuccessful(uint256 tokenId, uint256 price, address buyer);
    event SaleCancelled(uint256 tokenId);

    // Tracks last 5 sale price of gen0 kitty sales
    uint256 public gen0SaleCount;
    uint256[5] public lastGen0SalePrices;

    // Fee owner takes on each sale, measured in basis points (1/100 of a percent).
    // Values 0-10,000 map to 0%-100%
    uint256 internal saleFee;

    /*(function FishSale() public {
        saleFee = 500;
    }*/

    constructor () public {
        saleFee = 500;
    }

    /// @dev Computes owner's fee of a sale.
    /// @param _saleFee - Sale price of NFT.
    function setSaleFee(uint256 _saleFee) external onlyOwner {
        if (saleFee < 10000 && saleFee >= 0)
            saleFee = _saleFee;
    }

    /// @dev Computes owner's fee of a sale.
    /// @param _price - Sale price of NFT.
    function _computeFee(uint256 _price) internal view returns (uint256) {
        // NOTE: We don't use SafeMath (or similar) in this function because
        //  all of our entry functions carefully cap the maximum values for
        //  currency (at 128-bits), and saleFee <= 10000 (see the require()
        //  statement in the ClockAuction constructor). The result of this
        //  function is always guaranteed to be <= _price.
        return _price * saleFee / 10000;
    }

    function onSaleTokens() external view returns(uint[]){

            
        uint256 totalfishs = totalSupply();
        uint256 resultIndex = 0;
        uint256 num = 0;
        // We count on the fact that all fishes have IDs starting at 1 and increasing
        // sequentially up to the totalfish count.
        uint256 fishId;
        for (fishId = 0; fishId < totalfishs; fishId++) {
            if (tokenIdToSale[fishId].startedAt != 0) {
                // means it is not NULL
                //result[resultIndex] = fishId;
                num++;
            }
        }
        // query twice to init an array
        uint256[] memory result = new uint256[](num);
        for (fishId = 0; fishId < totalfishs; fishId++) {
            if (tokenIdToSale[fishId].startedAt != 0) {
                // means it is not NULL
                result[resultIndex] = fishId;
                resultIndex++;
            }
        }

        return result;
    }

    /// @dev Adds an sale to the list of open sales. Also fires the
    ///  SaleCreated event.
    /// @param _tokenId The ID of the token to be put on sale.
    /// @param _sale Sale to add.
    function _addSale(uint256 _tokenId, Sale _sale) internal {

        tokenIdToSale[_tokenId] = _sale;


        emit SaleCreated(
            uint256(_tokenId),
            uint256(_sale.price)
        );
    }

    /// @dev Creates and begins a new sale.
    /// @param _tokenId - ID of token to sale, sender must be owner.
    /// @param _price - Price of item (in wei) of sale.
    /// Seller, is the message sender
    function createSale(
        uint256 _tokenId,
        uint256 _price
    )
        external
        whenNotPaused
        onlyOwnerOf(_tokenId)
    {
        // Sanity check that no inputs overflow how many bits we've allocated
        // to store them in the sale struct.
        require(_price == uint256(uint128(_price)));

        Sale memory sale = Sale(
            msg.sender,
            uint128(_price),
            uint64(block.timestamp)
        );
        _addSale(_tokenId, sale);
    }

    function _createSale(
        uint256 _tokenId,
        uint256 _price,
        address _seller
    )
        internal
        whenNotPaused
        onlyOwnerOf(_tokenId)
    {
        // Sanity check that no inputs overflow how many bits we've allocated
        // to store them in the sale struct.
        require(_price == uint256(uint128(_price)));

        Sale memory sale = Sale(
            _seller,
            uint128(_price),
            uint64(block.timestamp)
        );
        _addSale(_tokenId, sale);
    }

    /// @dev Removes an sale from the list of open sales.
    /// @param _tokenId - ID of NFT on sale.
    function _removeSale(uint256 _tokenId) internal {
        delete tokenIdToSale[_tokenId];
    }

    /// @dev Cancels an sale unconditionally.
    /// @param _tokenId - ID of NFT on sale.
    
    function _cancelSale(uint256 _tokenId) internal {
        _removeSale(_tokenId);
        emit SaleCancelled(_tokenId);
    }

    /// @dev Returns true if the NFT is on sale.
    /// @param _sale - Sale to check.
    function _isOnSale(Sale storage _sale) internal view returns (bool) {
        return (_sale.startedAt > 0);
    }

    ///  Returns the NFT to original owner.
    /// @notice This is a state-modifying function that can
    ///  be called while the contract is paused.
    /// @param _tokenId - ID of token on sale
    function cancelSale(uint256 _tokenId)
        external
        onlyOwnerOf(_tokenId)
    {
        Sale storage sale = tokenIdToSale[_tokenId];
        require(_isOnSale(sale));
        _cancelSale(_tokenId);
    }

       /// @dev Computes the price and transfers ownership.
    function _buyOwnership(uint256 _tokenId, uint256 _buyOwnershipAmount)
        internal
        returns (uint256)
    {
        // Get a reference to the sale struct
        Sale storage sale = tokenIdToSale[_tokenId];

        // Explicitly check that this sale is currently live.
        // (Because of how Ethereum mappings work, we can't just count
        // on the lookup above failing. An invalid _tokenId will just
        // return an sale object that is all zeros.)
        require(_isOnSale(sale));

        // Check that amount is bigger or equal to the current price
        uint256 price = sale.price;
        require(_buyOwnershipAmount >= price);

        // Grab a reference to the seller before the sale struct
        // gets deleted.
        address seller = sale.seller;

        // The sale is good! Remove the sale before sending the fees
        // to the sender so we can't have a reentrancy attack.
        _removeSale(_tokenId);

        // Transfer proceeds to seller (if there are any!)
        if (price > 0) {
            // Calculate the transaction fee to contract address
            // (NOTE: _computeFee() is guaranteed to return a
            // value <= price, so this subtraction can't go negative.)
            uint256 saleeerFee = _computeFee(price);
            uint256 sellerProceeds = price - saleeerFee;

            // NOTE: Doing a transfer() in the middle of a complex
            // method like this is generally discouraged because of
            // reentrancy attacks and DoS attacks if the seller is
            // a contract with an invalid fallback function. We explicitly
            // guard against reentrancy attacks by removing the sale
            // before calling transfer(), and the only thing the seller
            // can DoS is the sale of their own asset! (And if it's an
            // accident, they can call cancelSale(). )
            seller.transfer(sellerProceeds);
        }

        Fish storage f = fishes[_tokenId];

        if (f.generation == 0) {
            lastGen0SalePrices[gen0SaleCount % 5] = price;
            gen0SaleCount++;
        }


        // Calculate any excess funds included with the bid. If the excess
        // is anything worth worrying about, transfer it back to bidder.
        // NOTE: We checked above that the bid amount is greater than or
        // equal to the price so this cannot underflow.
        uint256 bidExcess = _buyOwnershipAmount - price;

        // Return the funds. Similar to the previous transfer, this is
        // not susceptible to a re-entry attack because the sale is
        // removed before any transfers occur.
        msg.sender.transfer(bidExcess);

        // Tell the world!
        emit SaleSuccessful(_tokenId, price, msg.sender);

        return price;
    }

    /// @dev buyOwnership an open sale, completing the sale and transferring
    ///  ownership of the NFT if enough Ether is supplied.
    /// @param _tokenId - ID of token to buy.
    function buyOwnership(uint256 _tokenId)
        external
        payable
        whenNotPaused
    {   
        // Get a reference to the sale struct
        Sale storage sale = tokenIdToSale[_tokenId];
        address seller = sale.seller;

        // _buy will throw if the buy or funds transfer fails
        _buyOwnership(_tokenId, msg.value);
        // seller address
        
        // transfer happens from seller to buyer
        _transfer(seller,msg.sender, _tokenId);
    }


    function averageGen0SalePrice() internal view returns (uint256) {
        uint256 sum = 0;
        for (uint256 i = 0; i < 5; i++) {
            sum += lastGen0SalePrices[i];
        }
        return sum / 5;
    }
    


}



//////////
//////////
//////////          Contract FishBreedingSale
//////////
//////////


contract FishBreedingSale is FishSale{

      // Represents an sale on an NFT
    struct BreedingSale {
        // Current owner of NFT
        address seller;
        // Price (in wei) of sale
        uint128 price;
        // Time when sale started
        // NOTE: 0 if this sale has been concluded
        uint64 startedAt;
    }
    // NFT
    mapping (uint256 => BreedingSale) public tokenIdToBreedingSale;

    event BreedingSaleCreated(uint256 tokenId, uint256 price);
    event BreedingSaleSuccessful(uint256 tokenId, uint256 price, address buyer);
    event BreedingSaleCancelled(uint256 tokenId);

    // Fee owner takes on each sale, measured in basis points (1/100 of a percent).
    // Values 0-10,000 map to 0%-100%
    uint256 internal breedingSaleFee;

    


    /// constructor
    /// 


    /*function FishBreedingSale() public {
        breedingSaleFee = 200;
    }*/
    constructor() public{
        breedingSaleFee = 200;
    }

    /// @dev Computes owner's fee of a sale.
    /// @param _saleFee - Sale price of NFT.
    function setBreedingSaleFee(uint256 _saleFee) external onlyOwner {
        if (breedingSaleFee < 10000 && breedingSaleFee >= 0)
            breedingSaleFee = _saleFee;
    }

    

    /// @dev Computes owner's fee of a sale.
    /// @param _price - Sale price of NFT.
    function _computeFee(uint256 _price) internal view returns (uint256) {
        // NOTE: We don't use SafeMath (or similar) in this function because
        //  all of our entry functions carefully cap the maximum values for
        //  currency (at 128-bits), and saleFee <= 10000 (see the require()
        //  statement in the ClockAuction constructor). The result of this
        //  function is always guaranteed to be <= _price.
        return _price * breedingSaleFee / 10000;
    }


    function onBreedingsSaleTokens() external view returns(uint[]){

            
        uint256 totalfishs = totalSupply();
        uint256 resultIndex = 0;
        uint256 num = 0;
        // We count on the fact that all fishes have IDs starting at 1 and increasing
        // sequentially up to the totalfish count.
        uint256 fishId;
        for (fishId = 0; fishId < totalfishs; fishId++) {
            if (tokenIdToBreedingSale[fishId].startedAt != 0) {
                // means it is not NULL
                //result[resultIndex] = fishId;
                num++;
            }
        }
        // query twice to init an array
        uint256[] memory result = new uint256[](num);
        for (fishId = 0; fishId < totalfishs; fishId++) {
            if (tokenIdToBreedingSale[fishId].startedAt != 0) {
                // means it is not NULL
                result[resultIndex] = fishId;
                resultIndex++;
            }
        }

        return result;
    }



    /// @dev Adds an sale to the list of open sales. Also fires the
    ///  SaleCreated event.
    /// @param _tokenId The ID of the token to be put on sale.
    /// @param _sale Sale to add.
    function _addBreedingSale(uint256 _tokenId, BreedingSale _sale) internal {
        
        tokenIdToBreedingSale[_tokenId] = _sale;

        emit BreedingSaleCreated(
            uint256(_tokenId),
            uint256(_sale.price)
        );
    }
    
     /// @dev Checks that a given kitten is able to breed. Requires that the
    ///  current cooldown is finished (for sires) and also checks that there is
    ///  no pending pregnancy.
    function _isReadyToBreed(Fish _kit) internal view returns (bool) {
        // In addition to checking the cooldownEndBlock, we also need to check to see if
        // the fish has a pending birth; there can be some period of time between the end
        // of the pregnacy timer and the birth event.
        return (_kit.siringWithId == 0) && (_kit.cooldownEndBlock <= uint64(block.number));
    }

    /// @notice Checks that a given kitten is able to breed (i.e. it is not pregnant or
    ///  in the middle of a siring cooldown).
    /// @param _FishId reference the id of the kitten, any user can inquire about it
    function isReadyToBreed(uint256 _FishId)
        public
        view
        returns (bool)
    {
        require(_FishId > 0);
        Fish storage kit = fishes[_FishId];
        return _isReadyToBreed(kit);
    }

    /// @dev Creates and begins a new sale.
    /// @param _tokenId - ID of token to sale, sender must be owner.
    /// @param _price - Price of item (in wei) of sale.
    /// - Seller is the message sender
    function createBreedingSale(
        uint256 _tokenId,
        uint256 _price
    )
        external
        whenNotPaused
        onlyOwnerOf(_tokenId)
    {
        // Sanity check that no inputs overflow how many bits we've allocated
        // to store them in the sale struct.
        require(_price == uint256(uint128(_price)));
        
        require(isReadyToBreed(_tokenId));

        BreedingSale memory breedingSale = BreedingSale(
            msg.sender,
            uint128(_price),
            uint64(block.timestamp)
        );
        _addBreedingSale(_tokenId, breedingSale);
    }

    /// @dev Removes an sale from the list of open sales.
    /// @param _tokenId - ID of NFT on sale.
    function _removeBreedingSale(uint256 _tokenId) internal {
        delete tokenIdToBreedingSale[_tokenId];
    }

    /// @dev Cancels an sale unconditionally.
    /// @param _tokenId - ID of NFT on sale.
    
    function _cancelBreedingSale(uint256 _tokenId) internal {
        _removeBreedingSale(_tokenId);
        emit BreedingSaleCancelled(_tokenId);
    }

    /// @dev Returns true if the NFT is on sale.
    /// @param _sale - Sale to check.
    function _isOnBreedingSale(BreedingSale storage _sale) internal view returns (bool) {
        return (_sale.startedAt > 0);
    }

    ///  Returns the NFT to original owner.
    /// @notice This is a state-modifying function that can
    ///  be called while the contract is paused.
    /// @param _tokenId - ID of token on sale
    function cancelBreedingSale(uint256 _tokenId)
        external
        onlyOwnerOf(_tokenId)
    {
        BreedingSale storage sale = tokenIdToBreedingSale[_tokenId];
        require(_isOnBreedingSale(sale));
        _cancelBreedingSale(_tokenId);
    }


}



//////////
//////////
//////////          Contract GeneConsoleInterface
//////////
//////////


contract GeneConsoleInterface {

    /// @dev simply a boolean to indifishe this is the contract we expect to be
    function isGeneConsole() public pure returns (bool);
    /// @dev given genes of kitten 1 & 2, return a genetic combination - may have a random factor
    /// @param genes1 genes of mom
    /// @param genes2 genes of sire
    /// @return the genes that are supposed to be passed down the child
    function mixGenes(uint256 genes1, uint256 genes2, uint256 targetBlock) public pure returns (uint256);
}


//////////
//////////
//////////          Contract FishBreeding
//////////
//////////




/***
    functions for FishBreeding contract:
// procedures for breeding a new fish
    1. someone A put a fish's F1 mating on sale.
    2. someone B wants a fish F2 to mate with F1.
    3. B buys the mating right from A. payable. 
    4. create a new fish Fn accroding to the gene of F1 and F2.
        - call GeneticConsole to get the mixed gene
        - assign the owner of fn to B. 
        - trigger cooldown . coolIndex ++ for A and B. 
 */

/// @title A facet of FishCore that manages Fish siring, gestation, and birth.
/// @author Axiom Zen (https://www.axiomzen.co)
/// @dev See the FishCore contract documentation to understand how the various contract facets are arranged.
contract FishBreeding is FishBreedingSale {


    /// @dev The address of the sibling contract that is used to implement the sooper-sekret
    ///  genetic combination algorithm.
    GeneConsoleInterface public geneScience;
    
    

    /// @dev Update the address of the genetic contract, can only be called by the Owner of contract.
    ///  @param _address An address of a GeneScience contract instance to be used from this point forward.
    function setGeneScienceAddress(address _address) external onlyOwner {
        GeneConsoleInterface candidateContract = GeneConsoleInterface(_address);

        // NOTE: verify that a contract is what we expect - https://github.com/Lunyr/crowdsale-contracts/blob/cfadd15986c30521d8ba7d5b6f57b4fefcc7ac38/contracts/LunyrToken.sol#L117
        require(candidateContract.isGeneConsole());

        // Set the new contract address
        geneScience = candidateContract;
    }
    
    
    /// @param _to The address of the recipient, can be a user or contract.
    /// @param _tokenId The ID of the Fish to transfer.
    /// @dev Required for ERC-721 compliance.
    function transfer(
        address _to,
        uint256 _tokenId
    )
        public
        whenNotPaused
        onlyOwnerOf(_tokenId)
    {
        // Safety check to prevent against an unexpected 0x0 default.
        require(_to != address(0));

        // make sure the one transferred cannot be on sale or BreedingSale at the same time
        require(tokenIdToSale[_tokenId].startedAt==0);
        require(tokenIdToBreedingSale[_tokenId].startedAt==0);

        // Reassign ownership, clear pending approvals, emit Transfer event.
        _transfer(msg.sender, _to, _tokenId);
    }


    function _tool_breed(uint _mateId,uint _tokenId) internal returns(uint)
    {
        // mix genes from matron and sire.

        // Grab a reference to the matron in storage.
        Fish storage matron = fishes[_mateId];
        Fish storage sire = fishes[_tokenId];
        
        ///// checks requirements
        
        require(_isReadyToBreed(matron) && _isReadyToBreed(sire));

        require(_canBreedWithViaAuction(_mateId,_tokenId));
        

        uint256 mixedGene = geneScience.mixGenes(matron.genes,sire.genes,block.number);


        // Determine the higher generation number of the two parents
        uint16 parentGen = matron.generation;
        if (sire.generation > matron.generation) {
            parentGen = sire.generation;
        }

        // Make the new fish!
        address owner = fishToOwner[_mateId];
        uint256 newFishId = _createFish(mixedGene, uint32(_mateId), uint32(_tokenId),uint16(parentGen + 1), owner);

         // Trigger the cooldown for both parents.
        _triggerCooldown(sire);
        _triggerCooldown(matron);

        return newFishId;
    }


    /// @dev buy Usage an open sale, completing the sale and transferring
    ///  Usage of the NFT if enough Ether is supplied.
    /// @param _tokenId - ID of token to buy.
    /// @param _mateId - ID of token owned
    function buyMating(uint256 _tokenId,uint256 _mateId)
        external
        onlyOwnerOf(_mateId)
        payable
        whenNotPaused
        returns(uint256 newFishID)
    {
        // _buy will throw if the buy or funds transfer fails
        //_buyMating(_tokenId, msg.value);
        // seller address


        //      CHECKS

        // get price.
        BreedingSale storage sale = tokenIdToBreedingSale[_tokenId];
        
        // Check that amount is bigger or equal to the current price
        uint256 price = sale.price;

        require(price <= msg.value);

         // Explicitly check that this sale is currently live.
        // (Because of how Ethereum mappings work, we can't just count
        // on the lookup above failing. An invalid _tokenId will just
        // return an sale object that is all zeros.)
        require(_isOnBreedingSale(sale));

        

        //      PROCESSING

        // Grab a reference to the seller before the sale struct
        // gets deleted.
        address seller = sale.seller;

        // The sale is good! Remove the sale before sending the fees
        // to the sender so we can't have a reentrancy attack.
        _removeBreedingSale(_tokenId);

        uint256 newFishId = _tool_breed(_mateId,_tokenId);

        // Clear the reference to sire from the matron (REQUIRED! Having siringWithId
        // set is what marks a matron as being pregnant.)
        // notice : we dont have pregant peroird
        ////delete matron.siringWithId;

        //          TRANSFER

        // Send the balance fee to the person who offered the dad (sireId or tokenId).  
        // Transfer proceeds to seller (if there are any!)
        if (price > 0) {
            // Calculate the transaction fee to contract address
            // (NOTE: _computeFee() is guaranteed to return a
            // value <= price, so this subtraction can't go negative.)
            uint256 saleeerFee = _computeFee(price);
            //uint256 sellerProceeds = ;

            // NOTE: Doing a transfer() in the middle of a complex
            // method like this is generally discouraged because of
            // reentrancy attacks and DoS attacks if the seller is
            // a contract with an invalid fallback function. We explicitly
            // guard against reentrancy attacks by removing the sale
            // before calling transfer(), and the only thing the seller
            // can DoS is the sale of their own asset! (And if it's an
            // accident, they can call cancelSale(). )
            seller.transfer(price - saleeerFee);
        }
        // Tell the world!
        
        emit BreedingSaleSuccessful(_tokenId, price, msg.sender);
        return newFishId;

    }



   

    /// @dev Set the cooldownEndTime for the given Fish, based on its current cooldownIndex.
    ///  Also increments the cooldownIndex (unless it has hit the cap).
    /// @param _fish A reference to the Fish in storage which needs its timer started.
    function _triggerCooldown(Fish storage _fish) internal {
        // Compute an estimation of the cooldown time in blocks (based on current cooldownIndex).
        _fish.cooldownEndBlock = uint64((cooldowns[_fish.cooldownIndex]/secondsPerBlock) + block.number);

        // Increment the breeding count, clamping it at 13, which is the length of the
        // cooldowns array. We could check the array size dynamically, but hard-coding
        // this as a constant saves gas. Yay, Solidity!
        if (_fish.cooldownIndex < 13) {
            _fish.cooldownIndex += 1;
        }
    }

    

    /// @dev Internal check to see if a given sire and matron are a valid mating pair. DOES NOT
    ///  check ownership permissions (that is up to the caller).
    /// @param _matron A reference to the Fish struct of the potential matron.
    /// @param _matronId The matron's ID.
    /// @param _sire A reference to the Fish struct of the potential sire.
    /// @param _sireId The sire's ID
    function _isValidMatingPair(
        Fish storage _matron,
        uint256 _matronId,
        Fish storage _sire,
        uint256 _sireId
    )
        private
        view
        returns(bool)
    {
        // A Fish can't breed with itself!
        if (_matronId == _sireId) {
            return false;
        }

        // fishes can't breed with their parents.
        if (_matron.matronId == _sireId || _matron.sireId == _sireId) {
            return false;
        }
        if (_sire.matronId == _matronId || _sire.sireId == _matronId) {
            return false;
        }

        // We can short circuit the sibling check (below) if either fish is
        // gen zero (has a matron ID of zero).
        if (_sire.matronId == 0 || _matron.matronId == 0) {
            return true;
        }

        // fishes can't breed with full or half siblings.
        if (_sire.matronId == _matron.matronId || _sire.matronId == _matron.sireId) {
            return false;
        }
        if (_sire.sireId == _matron.matronId || _sire.sireId == _matron.sireId) {
            return false;
        }

        // Everything seems cool! Let's get DTF.
        return true;
    }

    /// @dev Internal check to see if a given sire and matron are a valid mating pair for
    ///  breeding via auction (i.e. skips ownership and siring approval checks).
    function _canBreedWithViaAuction(uint256 _matronId, uint256 _sireId)
        internal
        view
        returns (bool)
    {
        Fish storage matron = fishes[_matronId];
        Fish storage sire = fishes[_sireId];
        return _isValidMatingPair(matron, _matronId, sire, _sireId);
    }

}




//////////
//////////
//////////          Contract FishMinting
//////////
//////////


/// @title all functions related to creating fishs
contract FishMinting is FishBreeding {

    // Limits the number of fishs the contract owner can ever create.
    uint256 public constant PROMO_CREATION_LIMIT = 5000;
    uint256 public constant GEN0_CREATION_LIMIT = 45000;

    // Constants for gen0 auctions.
    uint256 public constant GEN0_STARTING_PRICE = 10 finney;

    // Counts the number of fishs the contract owner has created.
    uint256 public promoCreatedCount;
    uint256 public gen0CreatedCount;

    /// @dev we can create promo fishs, up to a limit. Only callable by COO
    /// @param _genes the encoded genes of the fish to be created, any value is accepted
    /// @param _owner the future owner of the created fishs. Default to contract COO
    function createPromofish(uint256 _genes, address _owner) external onlyOwner {
        address fishOwner = _owner;
        if (fishOwner == address(0)) {
            fishOwner = owner;
        }
        require(promoCreatedCount < PROMO_CREATION_LIMIT);

        promoCreatedCount++;
        _createFish(_genes,0, 0, 0, fishOwner);
    }


    /// @dev Creates a new gen0 fish with the random given genes and
    ///  creates an auction for it.
    function createRandomGen0Sale() external onlyOwner {
        require(gen0CreatedCount < GEN0_CREATION_LIMIT);

        uint256 _genes = uint256(keccak256(block.number, block.timestamp));

        uint256 fishId = _createFish(_genes,0, 0, 0, address(this));

        _createSale(
            fishId,
            _computeNextGen0Price(),
            address(this)
        );

        gen0CreatedCount++;
    }


    /// @dev Creates a new gen0 fish with the given genes and
    ///  creates an auction for it.
    function createGen0Sale(uint256 _genes) external onlyOwner {
        require(gen0CreatedCount < GEN0_CREATION_LIMIT);

        uint256 fishId = _createFish(_genes,0, 0, 0, address(this));

        _createSale(
            fishId,
            _computeNextGen0Price(),
            address(this)
        );

        gen0CreatedCount++;
    }

    /// @dev Computes the next gen0 auction starting price, given
    ///  the average of the past 5 prices + 50%.
    function _computeNextGen0Price() internal view returns (uint256) {
        uint256 avePrice = averageGen0SalePrice();

        // Sanity check to ensure we don't overflow arithmetic
        require(avePrice == uint256(uint128(avePrice)));

        uint256 nextPrice = avePrice + (avePrice / 2);

        // We never auction for less than starting price
        if (nextPrice < GEN0_STARTING_PRICE) {
            nextPrice = GEN0_STARTING_PRICE;
        }

        return nextPrice;
    }
}


//////////
//////////
//////////          Contract FishCore
//////////
//////////

/// @dev The main CryptoFishes contract, keeps track of fishs so they don't wander around and get lost.
contract FishCore is FishMinting {

         //community to breed, breed, breed!

    // Set in case the core contract is broken and an upgrade is required
    address public newContractAddress;

    /// Creates the main CryptoFishes smart contract instance.

    /*function FishCore() public {
        // Starts paused.
        paused = true;
        
        // start with the mythical fish 0 - so we don't have generation-0 parent issues
        _createFish(uint256(-1),0, 0, 0, address(0));
    }*/

    constructor () public {
        // Starts paused.
        //paused = true;
        
        // start with the mythical fish 0 - so we don't have generation-0 parent issues
        //
    }


    function init() public{
        _createFish(uint256(keccak256(block.timestamp)),0, 0, 0, address(0));
    }


  
    /// @notice Returns all the relevant information about a specific fish.
    /// @param _id The ID of the fish of interest.
    function getFish(uint256 _id)
        external
        view
        returns (
        bool isGestating,
        bool isReady,
        uint256 cooldownIndex,
        uint256 nextActionAt,
        uint256 siringWithId,
        uint256 birthTime,
        uint256 matronId,
        uint256 sireId,
        uint256 generation,
        uint256 genes
    ) {
        Fish storage kit = fishes[_id];

        // if this variable is 0 then it's not gestating
        isGestating = (kit.siringWithId != 0);
        isReady = (kit.cooldownEndBlock <= block.number);
        cooldownIndex = uint256(kit.cooldownIndex);
        nextActionAt = uint256(kit.cooldownEndBlock);
        siringWithId = uint256(kit.siringWithId);
        birthTime = uint256(kit.birthTime);
        matronId = uint256(kit.matronId);
        sireId = uint256(kit.sireId);
        generation = uint256(kit.generation);
        genes = kit.genes;
    }

    /// @dev Override unpause so it requires all external contract addresses
    ///  to be set before contract can be unpaused. Also, we can't have
    ///  newContractAddress set either, because then the contract was upgraded.
    /// @notice This is public rather than external so we can call super.unpause
    ///  without using an expensive CALL.
    function unpause() public onlyOwner whenPaused {
        require(geneScience != address(0));
        

        paused = false;
    }


}

contract GeneticConsole{
   
    /// @dev given genes of kitten 1 & 2, return a genetic combination - may have a random factor
    /// @param genes1 genes of mom
    /// @param genes2 genes of sire
    /// @return the genes that are supposed to be passed down the child
    function mixGenes(uint256 genes1, uint256 genes2, uint256 targetBlock) public pure returns (uint256)
    {
        //return uint(keccak256(targetBlock,genes1,genes2));
        return uint((genes1+genes2)/2+uint(keccak256(targetBlock)));
    }

    function isGeneConsole() public pure returns (bool)
    {
        return true;
    }

}
