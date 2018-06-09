pragma solidity ^0.4.19;


import "./FishBreedingSale.sol";

contract GeneConsoleInterface {

    /// @dev simply a boolean to indifishe this is the contract we expect to be
    function isGeneConsole() public pure returns (bool);
    /// @dev given genes of kitten 1 & 2, return a genetic combination - may have a random factor
    /// @param genes1 genes of mom
    /// @param genes2 genes of sire
    /// @return the genes that are supposed to be passed down the child
    function mixGenes(uint256 genes1, uint256 genes2, uint256 targetBlock) public returns (uint256);
}



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
    /// @param _address An address of a GeneScience contract instance to be used from this point forward.
    function setGeneScienceAddress(address _address) external onlyOwner {
        GeneConsoleInterface candidateContract = GeneConsoleInterface(_address);

        // NOTE: verify that a contract is what we expect - https://github.com/Lunyr/crowdsale-contracts/blob/cfadd15986c30521d8ba7d5b6f57b4fefcc7ac38/contracts/LunyrToken.sol#L117
        require(candidateContract.isGeneConsole());

        // Set the new contract address
        geneScience = candidateContract;
    }

    function _tool_breed(uint _mateId,uint _tokenId,uint parentGen) internal returns(uint)
    {
        // mix genes from matron and sire.
        uint256 mixedGene = geneScience.mixGenes(fishes[_mateId].genes,fishes[_tokenId].genes,block.number);

        // Make the new fish!
        address owner = fishToOwner[_mateId];
        uint256 newFishId = _createFish(mixedGene, uint32(_mateId), uint32(_tokenId),uint16(parentGen + 1), owner);
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


        // Grab a reference to the matron in storage.
        Fish storage matron = fishes[_mateId];
        Fish storage sire = fishes[_tokenId];

        // Check that amount is bigger or equal to the current price
        uint256 price = sale.price;

        require(price <= msg.value);

         // Explicitly check that this sale is currently live.
        // (Because of how Ethereum mappings work, we can't just count
        // on the lookup above failing. An invalid _tokenId will just
        // return an sale object that is all zeros.)
        require(_isOnBreedingSale(sale));

        require(_isReadyToBreed(matron) && _isReadyToBreed(sire));

        require(_canBreedWithViaAuction(_mateId,_tokenId));
        


        //      PROCESSING



        // Determine the higher generation number of the two parents
        uint16 parentGen = matron.generation;
        if (sire.generation > matron.generation) {
            parentGen = sire.generation;
        }


        // Grab a reference to the seller before the sale struct
        // gets deleted.
        address seller = sale.seller;

        // The sale is good! Remove the sale before sending the fees
        // to the sender so we can't have a reentrancy attack.
        _removeBreedingSale(_tokenId);


        uint256 newFishId = _tool_breed(_mateId,_tokenId,parentGen);

        // Clear the reference to sire from the matron (REQUIRED! Having siringWithId
        // set is what marks a matron as being pregnant.)
        // notice : we dont have pregant peroird
        delete matron.siringWithId;

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

        // Trigger the cooldown for both parents.
        _triggerCooldown(sire);
        _triggerCooldown(matron);

        // Tell the world!
        
        emit BreedingSaleSuccessful(_tokenId, price, msg.sender);
        return newFishId;

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

