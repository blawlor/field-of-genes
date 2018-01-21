zone=us-east1-b

gcloud container clusters delete -q fieldofgenes --zone $zone

mapfile -t disknames < <( gcloud compute disks list | grep $zone | awk '{print $1}' )
for diskname in "${disknames[@]}"
do
   echo About to delete $diskname
   gcloud compute disks delete $diskname --zone $zone --quiet
done

#=================================================================================
