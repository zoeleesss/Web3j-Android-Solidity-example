pragma solidity ^0.4.19;


contract Test {

    uint256 public gene;
    function createRandomGene() public {
        gene = uint(keccak256(block.number, now));
    }



}