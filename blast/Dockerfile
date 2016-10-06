#################################################################
# Dockerfile
#
# Version:          1
# Software:         NCBI BLAST+
# Software Version: 2.2.31-bl
# Description:      basic local alignment search tool
# Website:          http://blast.ncbi.nlm.nih.gov/Blast.cgi?PAGE_TYPE=BlastDocs&DOC_TYPE=Download
# Tags:             Genomics|Proteomics|Transcriptomics|General
# Provides:         blast 2.2.31
# Base Image:       blawlor/biodocker:2.2.31-bl
# Build Cmd:        docker build blawlor/blast 2.2.31/.
# Pull Cmd:         docker pull blawlor/blast
# Run Cmd:          docker run blawlor/blast <program>
#################################################################

# Source Image
FROM blawlor/biodocker:3-bl

################## BEGIN INSTALLATION ###########################

# install
RUN conda install blast=2.2.31

# Change workdir to /data/
WORKDIR /data/


##################### INSTALLATION END ##########################

# File Author / Maintainer
MAINTAINER Saulo Alves Aflitos <sauloal@gmail.com>
# Modified by Felipe da Veiga Leprevost 06-17-2016
