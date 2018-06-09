pragma solidity ^0.4.19;

import "./FishOwnership.sol";

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
    mapping (uint256 => Sale) tokenIdToSale;

    event SaleCreated(uint256 tokenId, uint256 price);
    event SaleSuccessful(uint256 tokenId, uint256 price, address buyer);
    event SaleCancelled(uint256 tokenId);

    // Tracks last 5 sale price of gen0 kitty sales
    uint256 public gen0SaleCount;
    uint256[5] public lastGen0SalePrices;

    // Fee owner takes on each sale, measured in basis points (1/100 of a percent).
    // Values 0-10,000 map to 0%-100%
    uint256 internal saleFee;

    function FishSale() public {
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

    /// @dev Adds an sale to the list of open sales. Also fires the
    ///  SaleCreated event.
    /// @param _tokenId The ID of the token to be put on sale.
    /// @param _sale Sale to add.
    function _addSale(uint256 _tokenId, Sale _sale) internal {
        
        tokenIdToSale[_tokenId] = _sale;

        SaleCreated(
            uint256(_tokenId),
            uint256(_sale.price)
        );
    }

    /// @dev Creates and begins a new sale.
    /// @param _tokenId - ID of token to sale, sender must be owner.
    /// @param _price - Price of item (in wei) of sale.
    /// @param _seller - Seller, if not the message sender
    function createSale(
        uint256 _tokenId,
        uint256 _price,
        address _seller
    )
        external
        whenNotPaused
        onlyOwnerOf(_tokenId)
    {
        // Sanity check that no inputs overflow how many bits we've allocated
        // to store them in the sale struct.
        require(_price == uint256(uint128(_price)));

        Sale memory sale = Sale(
            _seller,
            uint128(_price),
            uint64(now)
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
            uint64(now)
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
    /// @param _seller - seller of the token.
    function _cancelSale(uint256 _tokenId, address _seller) internal {
        _removeSale(_tokenId);
        SaleCancelled(_tokenId);
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
        address seller = sale.seller;
        _cancelSale(_tokenId, seller);
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
        SaleSuccessful(_tokenId, price, msg.sender);

        

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
        // _buy will throw if the buy or funds transfer fails
        _buyOwnership(_tokenId, msg.value);
        // seller address
        

        // transfer happens from seller to buyer
        transfer(msg.sender, _tokenId);
    }


    function averageGen0SalePrice() internal view returns (uint256) {
        uint256 sum = 0;
        for (uint256 i = 0; i < 5; i++) {
            sum += lastGen0SalePrices[i];
        }
        return sum / 5;
    }
    


}
