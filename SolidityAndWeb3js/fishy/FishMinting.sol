pragma solidity ^0.4.19;
import "./FishBreeding.sol";


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