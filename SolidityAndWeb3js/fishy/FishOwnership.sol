pragma solidity ^0.4.21;

import "./FishFactory.sol";
import "./Safemath.sol";
import "./ERC721.sol";

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

        // Reassign ownership, clear pending approvals, emit Transfer event.
        _transfer(msg.sender, _to, _tokenId);
    }

    /// @notice Returns the total number of Fishes currently in existence.
    /// @dev Required for ERC-721 compliance.
    function totalSupply() public view returns (uint) {
        return fishes.length - 1;
    }

    function approve(address _to, uint256 _tokenId) public whenNotPaused onlyOwnerOf(_tokenId) {
        fishToOwner[_tokenId] = _to;
        Approval(msg.sender, _to, _tokenId);
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

            for (fishId = 1; fishId <= totalfishs; fishId++) {
                if (fishToOwner[fishId] == _owner) {
                    result[resultIndex] = fishId;
                    resultIndex++;
                }
            }

            return result;
        }
    }
}