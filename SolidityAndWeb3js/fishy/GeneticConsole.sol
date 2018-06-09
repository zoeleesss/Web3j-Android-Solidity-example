pragma solidity ^0.4.21;

contract GeneticConsole{

    /// CONSTANTS HERE 

    uint64 ERR_NOT_A_PAIR = 12;
     // 100 / 1000 = 10% muatate one geno
    uint256 mutateProbablity = 100;

    // 10 / 1000 = 1% mutate two genos
    uint256 mutateDoubleProbablity = 10;

    /// CONSTANTS END

   
    function _random(uint256 _refactor) public view returns (uint256) {
        uint256 randomHash = uint256(keccak256(block.difficulty, now , _refactor));
        return randomHash % 1000;
    } 



/**
    * @notice genetic algorithm mixture for two binaries
    * @dev genetical mixture by mapping with biological probablities
    */

    function _mix_tool(uint256 gene1, uint256 gene2, uint256 _refactor) public view returns(uint256) {
        uint256 rand = _random(_refactor);
    
        // gene1 is 11
        if (gene1 == 3 ){
            //gene2 is also 11
            if (gene2 == 3 ){
                return 3;
            }
            // gene2 is 10 || 01
            else if ( gene2 == 2 || gene2 == 1){
                if ( rand < 500 ) return 3;
                else return 2;
            }
            // gene2 is 00 
            else if ( gene2 == 2 || gene2 == 1){
                return 2;
            }
            else return 0;
        } 

        // gene1 is 10 || 01
        else if (gene1 == 2 || gene1 == 1){
            // gene2 is 11
            if (gene2 == 3){
                if ( rand < 500 ) return 3;
                else return 2;
            }
            // gene2 is 10 || 01
            else if (gene2 == 2 || gene2 == 1){
                if (rand < 250) return 3;
                else if (rand < 750 ) return 2;
                else return 0;
            }
            // gene2 is 00
            else if (gene2 == 0){
                if ( rand < 500 ) return 2;
                else return 0;
            }
        }

        // gene1 is 00
        else if (gene1 == 0){
            // gene2 is 11
            if (gene2 == 2){
                return 10;
            }
            // gene2 is 10 || 01
            else if (gene2 == 2 || gene2 == 1){
                if ( rand < 500 ) return 2;
                else return 0;
            }
            // gene2 is 00
            else if (gene2 == 0){
                return 0;
            }
        }

        else return ERR_NOT_A_PAIR;

    }

    /**
    * @notice mix genes
    * @dev mix genes by the mixture of 128 binary pairs
    */

    function _mix(uint256 g1, uint256 g2) public view returns(uint256){
        uint256 result = 0;
        for (uint256 i = 0; i <= 127; i++){
            // now get the first two binaries
            uint256 temp1 = ( g1 >> i*2 ) & 3;
            uint256 temp2 = ( g2 >> i*2 ) & 3;
            // get the mixed result for the mixture of 2 binaries 
            uint256 r = _mix_tool(temp1,temp2,i);
            // set result
            result += r << i*2; 
        }
        return result;
    }

    /**
    * @notice mutate gene
    * @dev mutate one/two/zero gene(s) randomly
    */

    function _mutate(uint256 g1)public view returns(uint256){
        uint256 g = g1;
        uint256 rand = _random(block.number);
        if (rand < 10 ){
            // mutate double genos 
            g = g & ( uint256(1) << rand );
            g = g | ( uint256(1) << ( rand * mutateDoubleProbablity ));
            return g;
        }else if (rand < mutateProbablity / 2 ){
            // mutate one gene , with &
            g = g & ( uint256(1) << rand );
            return g;
        }else if (rand > (1000 - mutateProbablity / 2 )){
            // mutate one gene ,  with |
            g = g | ( uint256(1) << (1000 - rand) );
            return g;
        }else {
            // doen't mutate 
            return g;
        }
    
    }

    /**
    * @notice reproduce an offspring
    * @dev call _mix and _mutate functions
    */

    function reproduce(uint256 momGene, uint256 dadGene)public view returns(uint256){
        uint256 offspringGene = _mix(momGene, dadGene);
        offspringGene = _mutate(offspringGene);
        return offspringGene;
    }

    uint256 public mixGene1 = 99;   // 1100011
    uint256 public mixGene2 = 21;   // 0010101



    function testMix() public view returns(uint){
        return _mix(mixGene1, mixGene2);
    }

    function testReproduce() public view returns(uint){
        return reproduce(mixGene1, mixGene2);
    }

    function randMix() public view returns(uint){
        uint g1 = uint(keccak256(1+now));
        uint g2 = uint(keccak256(22+now));
        return reproduce(g1,g2);
    }
   
    /// @dev given genes of kitten 1 & 2, return a genetic combination - may have a random factor
    /// @param genes1 genes of mom
    /// @param genes2 genes of sire
    /// @return the genes that are supposed to be passed down the child
    function mixGenes(uint256 genes1, uint256 genes2, uint256 targetBlock) public returns (uint256)
    {
        //return uint(keccak256(targetBlock,genes1,genes2));
        return uint((genes1+genes2)/2+uint(keccak256(targetBlock)));
    }

    function isGeneConsole() public pure returns (bool)
    {
        return true;
    }

}
