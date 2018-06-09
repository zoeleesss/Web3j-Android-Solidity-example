pragma solidity ^0.4.19;

import "./FishSale.sol";

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
    mapping (uint256 => BreedingSale) tokenIdToBreedingSale;

    event BreedingSaleCreated(uint256 tokenId, uint256 price);
    event BreedingSaleSuccessful(uint256 tokenId, uint256 price, address buyer);
    event BreedingSaleCancelled(uint256 tokenId);

    // Fee owner takes on each sale, measured in basis points (1/100 of a percent).
    // Values 0-10,000 map to 0%-100%
    uint256 internal breedingSaleFee;


    /// constructor
    /// 


    function FishBreedingSale() public {
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

    /// @dev Creates and begins a new sale.
    /// @param _tokenId - ID of token to sale, sender must be owner.
    /// @param _price - Price of item (in wei) of sale.
    /// @param _seller - Seller, if not the message sender
    function createBreedingSale(
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

        BreedingSale memory breedingSale = BreedingSale(
            _seller,
            uint128(_price),
            uint64(now)
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
    /// @param _seller - seller of the token.
    function _cancelBreedingSale(uint256 _tokenId, address _seller) internal {
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
        address seller = sale.seller;
        _cancelBreedingSale(_tokenId, seller);
    }



}
