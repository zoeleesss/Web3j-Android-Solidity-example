pragma solidity ^0.4.19;

import "./Ownable.sol";
import "./Safemath.sol";

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
    mapping (address => uint256) ownerFishCount;

    /// @dev A mapping from FishIDs to an address that has been approved to call
    ///  transferFrom(). Each Fish can only have one approved address for transfer
    ///  at any time. A zero value means no approval is outstanding.
    mapping (uint256 => address) public fishApprovals;

    /// @dev A mapping from FishIDs to an address that has been approved to use
    ///  this Fish for siring via breedWith(). Each Fish can only have one approved
    ///  address for siring at any time. A zero value means no approval is outstanding.
    mapping (uint256 => address) public sireAllowedAddress;

    /// @dev Assigns ownership of a specific Fish to an address.
    function _transfer(address _from, address _to, uint256 _tokenId) internal {
        // Since the number of fishes is capped to 2^32 we can't overflow this
        ownerFishCount[_to].add(1);
        // transfer ownership
        fishToOwner[_tokenId] = _to;
        
        ownerFishCount[_from].sub(1);
        // once the fish is transferred also clear sire allowances
        delete sireAllowedAddress[_tokenId];
        // clear any previously approved ownership exchange
        delete fishApprovals[_tokenId];
        
        // Emit the transfer event.
        Transfer(_from, _to, _tokenId);
    }


    function _createFish(
        uint256 _genes,
        uint32 _matronId, 
        uint32 _sireId, 
        uint16 _generation,
        address _owner
        ) 
        internal 
        returns (uint256)
    {
        // New Fish starts with the same cooldown as parent gen/2
        uint16 cooldownIndex = uint16(_generation / 2);
        if (cooldownIndex > 13) {
            cooldownIndex = 13;
        }

        Fish memory _fish = Fish({
            genes: _genes,
            birthTime: uint64(now),
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
        require(_newFishId == uint256(newFishId));

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
